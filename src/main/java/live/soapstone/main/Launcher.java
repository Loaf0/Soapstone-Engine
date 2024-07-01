package live.soapstone.main;

import live.soapstone.core.EngineManager;
import live.soapstone.core.ILogic;
import live.soapstone.core.WindowManager;
import live.soapstone.core.utils.Consts;
import org.lwjgl.Version;

import java.awt.*;

public class Launcher {

    private static WindowManager window;
    private static TestGame game;


    public static void main(String[] args) {
        window = new WindowManager(Consts.TITLE, 640, 480, false);
        game = new TestGame();
        EngineManager engine = new EngineManager();

        try {
            engine.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WindowManager getWindow(){
        return window;
    }

    public static ILogic getGame() {
        return game;
    }
}
