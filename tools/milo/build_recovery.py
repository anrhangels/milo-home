#!/usr/bin/env python3
"""Build the known working ByeByeDPI launcher baseline with a FORWARD versionCode; never installs."""
from pathlib import Path
import argparse,io,os,re,shutil,subprocess,tempfile,zipfile

def main():
    p=argparse.ArgumentParser();p.add_argument('--code',type=int,required=True);p.add_argument('--after-code',type=int,required=True)
    p.add_argument('--name',required=True);p.add_argument('--out',type=Path,required=True);a=p.parse_args()
    if not a.after_code<a.code<=2_100_000_000: p.error('Recovery versionCode must exceed the installed code')
    if not re.fullmatch('[A-Za-z0-9][A-Za-z0-9._-]{0,79}',a.name): p.error('Invalid version name')
    root=Path(__file__).resolve().parents[2];base=Path(__file__).with_name('recovery-base.txt').read_text().strip()
    if not re.fullmatch('[a-f0-9]{40}',base): raise ValueError('Unexpected baseline ref')
    archive=subprocess.check_output(['git','-C',str(root),'archive','--format=zip',base,'launcher'])
    with tempfile.TemporaryDirectory(prefix='milo-recovery-') as temp:
        directory=Path(temp)
        with zipfile.ZipFile(io.BytesIO(archive)) as z:
            for item in z.infolist():
                if not (directory/item.filename).resolve().is_relative_to(directory.resolve()): raise ValueError('Unsafe archive path')
            z.extractall(directory)
        project=directory/'launcher';build=project/'app/build.gradle';text=build.read_text(encoding='utf-8')
        text,n=re.subn(r'versionCode\s+\d+',f'versionCode {a.code}',text,count=1);assert n==1
        text,n=re.subn(r"versionName '[^']+'",f"versionName '{a.name}'",text,count=1);assert n==1
        if os.environ.get('MILO_KEYSTORE'):
            text=text.replace('    buildTypes {','''    signingConfigs {
        recovery {
            storeFile file(System.getenv('MILO_KEYSTORE'))
            storePassword System.getenv('MILO_STORE_PASSWORD')
            keyAlias System.getenv('MILO_KEY_ALIAS')
            keyPassword System.getenv('MILO_KEY_PASSWORD')
        }
    }
    buildTypes {''').replace('signingConfig signingConfigs.debug','signingConfig signingConfigs.recovery')
        build.write_text(text,encoding='utf-8')
        sdk=os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT')
        if sdk: (project/'local.properties').write_text('sdk.dir='+sdk.replace('\\','/').replace(':','\\:')+'\n')
        wrapper=project/('gradlew.bat' if os.name=='nt' else 'gradlew')
        if os.name!='nt': wrapper.chmod(0o755)
        subprocess.check_call([str(wrapper),'-p',str(project),'--no-daemon','assembleRelease'])
        a.out.parent.mkdir(parents=True,exist_ok=True)
        shutil.copyfile(project/'app/build/outputs/apk/release/app-release.apk',a.out)
    print('Built forward recovery APK; verify its certificate against the installed app before any installation.')
if __name__=='__main__': main()
