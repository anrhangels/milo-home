#!/usr/bin/env python3
"""Reproducible publisher checks against actual signed APK files, not mocked verifier output."""
import argparse, subprocess, tempfile, zipfile
from pathlib import Path
from release import verify_apk

def main():
    p=argparse.ArgumentParser()
    p.add_argument('--tools',type=Path,required=True)
    p.add_argument('--production',type=Path,required=True)
    p.add_argument('--instrumented',type=Path,required=True)
    p.add_argument('--signer',required=True)
    p.add_argument('--test-signer',required=True)
    a=p.parse_args()
    verify_apk(a.production,100,'1.0.0',a.signer,a.tools)
    print('PASS valid signed production APK')
    def rejected(label,apk,code,name,signer):
        try: verify_apk(apk,code,name,signer,a.tools)
        except (ValueError,subprocess.CalledProcessError): print('PASS rejected '+label)
        else: raise AssertionError('Accepted '+label)
    rejected('wrong signer',a.production,100,'1.0.0','0'*64)
    rejected('wrong versionCode',a.production,101,'1.0.0',a.signer)
    rejected('wrong versionName',a.production,100,'9.0.0',a.signer)
    rejected('debuggable instrumentation build',a.instrumented,9,'0.0.0',a.test_signer)
    with tempfile.TemporaryDirectory() as tmp:
        corrupt=Path(tmp)/'corrupt.apk'
        with zipfile.ZipFile(a.production) as source,zipfile.ZipFile(corrupt,'w') as target:
            for entry in source.infolist():
                data=source.read(entry.filename)
                if entry.filename=='classes.dex': data=bytes([data[0]^1])+data[1:]
                target.writestr(entry,data)
        rejected('tampered APK bytes',corrupt,100,'1.0.0',a.signer)

if __name__=='__main__': main()
