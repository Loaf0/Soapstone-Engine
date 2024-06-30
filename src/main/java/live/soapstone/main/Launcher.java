package live.soapstone.main;

import live.soapstone.core.WindowManager;
import org.lwjgl.Version;

import java.awt.*;

public class Launcher {
    public static void main(String[] args) {
        System.out.print(Version.getVersion());

        WindowManager window = new WindowManager("Soapstone Engine", 640, 480, false);
        window.init();

        while(!window.windowShouldClose()) {
            window.update();
        }

        window.cleanup();
    }


}
