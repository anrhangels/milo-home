#!/usr/bin/env python3
"""Validate immutable release inputs and create metadata from a verified signed APK."""
import argparse, hashlib, json, os, re, subprocess
from pathlib import Path

START='<!-- milo-update:v1\n'; END='\n-->'
def validate(code,name,channel,track,variant=None):
    if not 1<=int(code)<=2_100_000_000: raise ValueError('versionCode outside Android range')
    pattern=r'\d+\.\d+\.\d+'+(r'-rc\.[1-9]\d*' if channel=='rc' else '')
    if not re.fullmatch(pattern,name): raise ValueError('Version name does not match channel')
    if channel not in ('stable','rc') or track not in ('production','test'): raise ValueError('Invalid release channel/track')
    if variant is not None and variant not in ('standard','byedpi'): raise ValueError('Invalid variant')
    return ('test-' if track=='test' else '')+((variant+'-') if variant else '')+'v'+name

def prior_max(releases,track,variant=None):
    highest=0
    for r in releases:
        if r.get('draft'): continue
        body=r.get('body') or ''; start=body.find(START); end=body.find(END,start+len(START))
        if start<0 or end<0: continue
        try:
            m=json.loads(body[start+len(START):end])
            records=m.get('editions',{}).values() if 'editions' in m else [m]
            for m in records:
                if m['track']==track and m['applicationId']=='com.gathin.tvhome' and (variant is None or m.get('variant','byedpi')==variant): highest=max(highest,int(m['versionCode']))
        except (ValueError,KeyError,TypeError): continue
    return highest

def metadata(apk,code,name,channel,track,tag,signer,notes,variant=None):
    if tag!=validate(code,name,channel,track,variant): raise ValueError('Unexpected release tag')
    if not re.fullmatch('[a-fA-F0-9]{64}',signer): raise ValueError('Invalid expected signer SHA-256')
    return {'schema':1,'tag':tag,'applicationId':'com.gathin.tvhome','versionCode':int(code),'versionName':name,
        'channel':channel,'track':track,'variant':variant or 'byedpi','minSdk':26,'abis':['universal'],'asset':apk.name,
        'size':apk.stat().st_size,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),
        'signerSha256':signer.lower(),'notes':notes[:600]}

def verify_apk(apk,code,name,signer,tools,variant=None):
    suffix='.bat' if os.name=='nt' else ''
    verified=subprocess.check_output([str(tools/('apksigner'+suffix)),'verify','--verbose','--print-certs',str(apk)],text=True,encoding='utf-8',errors='replace')
    certs=re.findall(r'Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]+)',verified)
    if len(certs)!=1 or certs[0].lower()!=signer.lower(): raise ValueError('APK signer mismatch')
    aapt='aapt.exe' if os.name=='nt' else 'aapt'
    badging=subprocess.check_output([str(tools/aapt),'dump','badging',str(apk)],text=True,encoding='utf-8')
    expected=f"package: name='com.gathin.tvhome' versionCode='{code}' versionName='{name}'"
    if expected not in badging or "sdkVersion:'26'" not in badging: raise ValueError('APK package/version/minSdk mismatch')
    if 'application-debuggable' in badging: raise ValueError('Instrumented/debuggable APK must never be published')
    if apk.stat().st_size>2*1024*1024: raise ValueError('APK exceeds 2 MiB fork budget')
    if variant is not None:
        xml=subprocess.check_output([str(tools/aapt),'dump','xmltree',str(apk),'AndroidManifest.xml'],text=True,encoding='utf-8')
        match=re.search(r'android:name[^\n]*="com\.milohome\.variant"[^\n]*\n\s*A: android:value[^\n]*="([^"]+)"',xml)
        if not match or match.group(1)!=variant: raise ValueError('APK variant mismatch')
        permission='io.github.romanvht.byedpi.permission.TV_CONTROL'
        if (permission in badging)!=(variant=='byedpi'): raise ValueError('Unexpected ByeByeDPI permission')
    return verified

def main():
    p=argparse.ArgumentParser();p.add_argument('--code',type=int,required=True);p.add_argument('--name',required=True)
    p.add_argument('--channel',choices=['stable','rc'],required=True);p.add_argument('--track',choices=['production','test'],default='production')
    p.add_argument('--variant',choices=['standard','byedpi'],default='byedpi')
    p.add_argument('--prior',type=Path);p.add_argument('--apk',type=Path);p.add_argument('--tools',type=Path)
    p.add_argument('--signer');p.add_argument('--notes',type=Path);p.add_argument('--out',type=Path,default=Path('release-output'))
    a=p.parse_args();tag=validate(a.code,a.name,a.channel,a.track,a.variant)
    if a.prior:
        prior=json.loads(a.prior.read_text(encoding='utf-8'))
        if prior and isinstance(prior[0],list): prior=[r for page in prior for r in page]
        if a.code<=prior_max(prior,a.track,a.variant): raise ValueError('versionCode must exceed all published builds in this track, including RC')
        if any(r.get('tag_name')==tag for r in prior): raise ValueError('Release tag already published')
    if a.apk:
        if not a.tools or not a.signer: p.error('--apk requires --tools and --signer')
        verify_apk(a.apk,a.code,a.name,a.signer,a.tools,a.variant)
        notes=a.notes.read_text(encoding='utf-8') if a.notes else 'Milo Home maintenance release.'
        m=metadata(a.apk,a.code,a.name,a.channel,a.track,tag,a.signer,notes,a.variant)
        a.out.mkdir(parents=True,exist_ok=True)
        (a.out/'update.json').write_text(json.dumps(m,indent=2)+'\n',encoding='utf-8')
        (a.out/(a.apk.name+'.sha256')).write_text(m['sha256']+'  '+a.apk.name+'\n',encoding='utf-8')
        (a.out/'release-notes.md').write_text(START+json.dumps(m,separators=(',',':'))+END+'\n\n# Milo Home '+a.name+'\n\n'+notes+'\n\nIndependent modification of [CleanYourTV](https://github.com/Gathin23/cleanyourtv). Update with the Android installer; do not uninstall your launcher. See docs/UPDATING.md.\n',encoding='utf-8')
    print(tag)
if __name__=='__main__': main()
