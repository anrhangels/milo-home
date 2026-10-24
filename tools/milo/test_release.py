import importlib.util,json,tempfile,unittest
from pathlib import Path
spec=importlib.util.spec_from_file_location('milo_release',Path(__file__).with_name('release.py'))
release=importlib.util.module_from_spec(spec);spec.loader.exec_module(release)
class ReleaseTest(unittest.TestCase):
    def test_stable_tag(self): self.assertEqual(release.validate(100,'1.0.0','stable','production'),'v1.0.0')
    def test_rc_tag(self): self.assertEqual(release.validate(101,'1.1.0-rc.1','rc','production'),'v1.1.0-rc.1')
    def test_test_track_tag(self): self.assertEqual(release.validate(9001,'1.0.0','stable','test'),'test-v1.0.0')
    def test_shell_input_rejected(self):
        for name in ['1.0.0; echo secret','$(pwd)','1.0.0\nmalicious','../../1.0.0']:
            with self.assertRaises(ValueError): release.validate(100,name,'stable','production')
    def test_rc_not_published_as_stable(self):
        with self.assertRaises(ValueError): release.validate(100,'1.0.0-rc.1','stable','production')
    def test_invalid_codes(self):
        for code in [-1,0,2_147_483_648]:
            with self.assertRaises(ValueError): release.validate(code,'1.0.0','stable','production')
    def test_max_code_includes_both_channels(self):
        records=[{'body':release.START+json.dumps({'track':'production','applicationId':'com.gathin.tvhome','versionCode':n})+release.END,'prerelease':n==101} for n in [100,101]]
        self.assertEqual(101,release.prior_max(records,'production'))
    def test_drafts_and_test_track_do_not_change_production_max(self):
        self.assertEqual(0,release.prior_max([{'draft':True,'body':release.START+'{}'+release.END}],'production'))
    def test_metadata_hashes_actual_bytes(self):
        with tempfile.TemporaryDirectory() as d:
            apk=Path(d)/'MiloHome.apk';apk.write_bytes(b'APK bytes')
            m=release.metadata(apk,100,'1.0.0','stable','test','test-v1.0.0','a'*64,'notes')
            self.assertEqual(9,m['size']);self.assertEqual(64,len(m['sha256']));self.assertEqual('test',m['track'])
if __name__=='__main__': unittest.main()
