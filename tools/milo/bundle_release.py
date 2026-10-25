#!/usr/bin/env python3
"""Publish metadata for two verified editions under one public stable version."""
import argparse, json, re
from pathlib import Path
import release

def bundle(records, name):
    channel='rc' if '-rc.' in name else 'stable'
    if set(records) != {'standard', 'byedpi'}:
        raise ValueError('Both editions are required')
    tag='v'+name
    codes=set(); signers=set()
    for variant,m in records.items():
        release.validate(m['versionCode'],name,channel,'production',variant)
        if m['versionName']!=name or m['variant']!=variant or m['channel']!=channel or m['track']!='production':
            raise ValueError('Inconsistent edition metadata')
        codes.add(m['versionCode']);signers.add(m['signerSha256'])
        m['tag']=tag
    if len(codes)!=1 or len(signers)!=1:
        raise ValueError('Editions must use the same version code and signer')
    # A legacy Standard client can still read the top-level record.
    result=dict(records['standard']);result['editions']=records
    return result

def main():
    p=argparse.ArgumentParser();p.add_argument('--code',type=int,required=True);p.add_argument('--name',required=True)
    p.add_argument('--prior',type=Path,required=True);p.add_argument('--out',type=Path)
    p.add_argument('--tools',type=Path);p.add_argument('--signer');p.add_argument('--notes',type=Path)
    a=p.parse_args();channel='rc' if '-rc.' in a.name else 'stable';release.validate(a.code,a.name,channel,'production','standard')
    prior=json.loads(a.prior.read_text(encoding='utf-8'))
    if prior and isinstance(prior[0],list):prior=[r for page in prior for r in page]
    if a.code<=release.prior_max(prior,'production'):
        raise ValueError('Code must exceed every published production edition, including Preview')
    if any(r.get('tag_name')=='v'+a.name for r in prior):raise ValueError('Tag already exists')
    if a.out:
        if not a.tools or not a.signer or not a.notes:p.error('Output requires tools, signer and notes')
        notes=a.notes.read_text(encoding='utf-8');records={}
        for variant in ('standard','byedpi'):
            apk=a.out/('Milo-Home.apk' if variant=='standard' else 'Milo-Home-ByeByeDPI.apk')
            release.verify_apk(apk,a.code,a.name,a.signer,a.tools,variant)
            m=release.metadata(apk,a.code,a.name,channel,'production',release.validate(a.code,a.name,channel,'production',variant),a.signer,notes,variant)
            records[variant]=m
            (a.out/(apk.name+'.sha256')).write_text(m['sha256']+'  '+apk.name+'\n',encoding='utf-8')
        result=bundle(records,a.name)
        (a.out/'update.json').write_text(json.dumps(result,indent=2)+'\n',encoding='utf-8')
        (a.out/'release-notes.md').write_text(release.START+json.dumps(result,separators=(',',':'))+release.END+'\n\n'+notes+'\n',encoding='utf-8')
    print('v'+a.name)
if __name__=='__main__':main()
