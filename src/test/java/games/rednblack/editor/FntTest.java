package games.rednblack.editor;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import games.rednblack.editor.proxy.ProjectManager;

public class FntTest {
    @Test
    public void test1() {
        File defFontFile = new File("D:\\gdx\\HyperLap2D\\assets\\style\\default-font-cn.fnt");
        try {
            new ProjectManager().getFntImages(new FileInputStream(defFontFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
