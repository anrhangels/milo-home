#!/usr/bin/env python3
"""Fail publication for accidental private files/data, without printing matched secrets."""
from pathlib import Path
import subprocess,re,sys
root=Path(__file__).resolve().parents[2]
base='3e34f40f42728284bbb3d4d7bea57c3f504c3b31'
changed=subprocess.check_output(['git','-C',str(root),'diff','--name-only',base,'-z']).decode().split('\0')
untracked=subprocess.check_output(['git','-C',str(root),'ls-files','--others','--exclude-standard','-z']).decode().split('\0')
files=set(changed+untracked)
problems=[]
for name in filter(None,files):
    p=root/name
    if not p.is_file(): continue
    if any(x in p.parts for x in ['private-preflight','private-signing','installation-state','AndroidTV-Backup-Haier']): problems.append(name+': private directory')
    if p.suffix.lower() in {'.jks','.keystore','.p12','.apk'} or p.name in {'adbkey','adbkey.pub','secrets.json'}: problems.append(name+': private/binary artifact')
    if p.stat().st_size>2_000_000: continue
    try: text=p.read_text(encoding='utf-8')
    except UnicodeError: continue
    # Match private host paths and credential formats; public example addresses upstream are retained.
    if re.search(r'C:[/\\]Users[/\\][A-Za-z0-9_-]+|(?:[0-9A-F]{2}:){5}[0-9A-F]{2}|192\.168\.\d{1,3}\.\d{1,3}(?:\D|$)|gh[pousr]_[A-Za-z0-9]{25,}|github_pat_[A-Za-z0-9_]+|BEGIN (?:RSA |EC )?PRIVATE KEY',text,re.I): problems.append(name+': private data pattern')
if problems:
    print('\n'.join(problems));sys.exit(1)
print('Public tree scan passed; no matching private files or credential patterns.')
