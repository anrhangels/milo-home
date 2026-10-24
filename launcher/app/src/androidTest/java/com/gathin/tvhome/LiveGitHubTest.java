package com.gathin.tvhome;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONArray;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class LiveGitHubTest {
    @Test public void unauthenticatedRestTlsWorks() throws Exception {
        String body=UpdateHttp.text("https://api.github.com/repos/Gathin23/cleanyourtv/releases?per_page=1");
        assertNotNull(new JSONArray(body));
    }
}
