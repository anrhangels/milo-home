#!/usr/bin/env python3
"""Sign a locally built backend without placing private credentials in the repository."""
import argparse,os,subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--input',type=Path,required=True);p.add_argument('--output',type=Path,required=True);p.add_argument('--test',action='store_true');a=p.parse_args()
sdk=Path(os.environ['ANDROID_HOME']);tools=sdk/'build-tools/35.0.0';suffix='.bat' if os.name=='nt' else ''
env=os.environ.copy()
if a.test:
    store=Path.home()/'.android/debug.keystore';store.parent.mkdir(parents=True,exist_ok=True)
    if not store.exists():
        keytool=Path(os.environ['JAVA_HOME'])/'bin'/('keytool.exe' if os.name=='nt' else 'keytool')
        subprocess.run([str(keytool),'-genkeypair','-keystore',str(store),'-storepass','android','-keypass','android','-alias','androiddebugkey','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Android Debug'],check=True)
    alias='androiddebugkey';env['BACKEND_STORE_PASS']='android';env['BACKEND_KEY_PASS']='android'
else:
    store=Path(env['MILO_KEYSTORE']);alias=env['MILO_KEY_ALIAS']
    env['BACKEND_STORE_PASS']=env['MILO_STORE_PASSWORD'];env['BACKEND_KEY_PASS']=env['MILO_KEY_PASSWORD']
a.output.parent.mkdir(parents=True,exist_ok=True)
subprocess.run([str(tools/('apksigner'+suffix)),'sign','--ks',str(store),'--ks-key-alias',alias,'--ks-pass','env:BACKEND_STORE_PASS','--key-pass','env:BACKEND_KEY_PASS','--out',str(a.output),str(a.input)],env=env,check=True)
subprocess.run([str(tools/('apksigner'+suffix)),'verify',str(a.output)],check=True)
print('Paired backend signed and verified.')
