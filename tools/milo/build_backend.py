#!/usr/bin/env python3
"""Build bundled GPL backend from the checked-in corresponding source (no signing secrets)."""
import argparse,os,subprocess,zipfile
from pathlib import Path

def prepare(root,out):
    with zipfile.ZipFile(root/'integrations/byebye-dpi/source.zip') as z:
        for info in z.infolist():
            target=(out/info.filename).resolve()
            if not target.is_relative_to(out.resolve()):raise ValueError('Unsafe source path')
        z.extractall(out)
    project=out/'ByeByeDPI'
    for p in project.rglob('*'):
        if p.is_file() and (p.suffix in {'.kts','.properties','.xml','.kt','.java','.txt','.mk','.cmake','.sh'} or p.name=='gradlew'):
            data=p.read_bytes().replace(b'\r\r\n',b'\n').replace(b'\r\n',b'\n');p.write_bytes(data)
    build=project/'app/build.gradle.kts';s=build.read_text()
    s=s.replace('setOf("armeabi-v7a", "x86_64")','setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")')
    s=s.replace('APP_ABI=armeabi-v7a x86_64','APP_ABI=armeabi-v7a arm64-v8a x86 x86_64')
    s=s.replace('versionCode = 1820','versionCode = 1821').replace('versionName = "1.8.2"','versionName = "1.8.2-milo.1"')
    build.write_text(s)
    wrapper=project/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if os.name!='nt':wrapper.chmod(0o755)
    return project,wrapper

def main():
    p=argparse.ArgumentParser();p.add_argument('--out',type=Path,required=True);a=p.parse_args()
    root=Path(__file__).resolve().parents[2];project,wrapper=prepare(root,a.out.resolve())
    subprocess.run([str(wrapper),'--no-daemon','testReleaseUnitTest','assembleRelease'],cwd=project,check=True)
    apk=project/'app/build/outputs/apk/release/app-universal-release-unsigned.apk'
    if not apk.is_file():raise ValueError('No universal backend APK')
    print(apk)
if __name__=='__main__':main()
