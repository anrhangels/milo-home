import tempfile,unittest,zipfile
from pathlib import Path
import build_backend
class BackendSourceTest(unittest.TestCase):
    def test_corresponding_source_contains_no_apks_or_private_keys(self):
        root=Path(__file__).resolve().parents[2]
        with zipfile.ZipFile(root/'integrations/byebye-dpi/source.zip') as source:
            self.assertIn('ByeByeDPI/LICENSE',source.namelist())
            self.assertTrue(any('/tv/TvControlService.kt' in n for n in source.namelist()))
            self.assertFalse(any(n.endswith(('.apk','.so','.jks','.p12','.keystore','local.properties')) for n in source.namelist()))
    def test_prepared_source_has_all_abis_and_expected_identity(self):
        root=Path(__file__).resolve().parents[2]
        with tempfile.TemporaryDirectory() as temporary:
            project,wrapper=build_backend.prepare(root,Path(temporary))
            text=(project/'app/build.gradle.kts').read_text()
            for abi in ('armeabi-v7a','arm64-v8a','x86','x86_64'):self.assertIn(abi,text)
            self.assertIn('versionCode = 1821',text)
            self.assertIn('APP_ABI=armeabi-v7a arm64-v8a x86 x86_64',text)
            self.assertNotIn('\r',wrapper.read_text())
