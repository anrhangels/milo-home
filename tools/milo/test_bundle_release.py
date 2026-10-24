import importlib.util,json,sys,unittest
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parent))
import release,bundle_release
class BundleTest(unittest.TestCase):
    def records(self):
        return {v:dict(versionCode=104,versionName='0.0.3',variant=v,channel='stable',track='production',signerSha256='a'*64,applicationId='com.gathin.tvhome') for v in ('standard','byedpi')}
    def test_one_public_tag_both_editions(self):
        m=bundle_release.bundle(self.records(),'0.0.3')
        self.assertEqual('v0.0.3',m['tag'])
        self.assertEqual('standard',m['variant'])
        self.assertEqual({'standard','byedpi'},set(m['editions']))
        self.assertTrue(all(x['tag']=='v0.0.3' for x in m['editions'].values()))
    def test_different_codes_or_keys_rejected(self):
        for key,value in [('versionCode',105),('signerSha256','b'*64),('variant','standard')]:
            records=self.records();records['byedpi'][key]=value
            with self.assertRaises(ValueError):bundle_release.bundle(records,'0.0.3')
    def test_both_editions_count_toward_monotonic_floor(self):
        m=bundle_release.bundle(self.records(),'0.0.3')
        records=[{'body':release.START+json.dumps(m)+release.END}]
        self.assertEqual(104,release.prior_max(records,'production','byedpi'))
        self.assertEqual(104,release.prior_max(records,'production','standard'))
