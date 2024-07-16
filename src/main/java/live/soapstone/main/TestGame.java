package live.soapstone.main;

import live.soapstone.core.*;
import live.soapstone.core.entity.Entity;
import live.soapstone.core.entity.Model;
import live.soapstone.core.entity.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import static live.soapstone.core.utils.Consts.*;

public class TestGame implements ILogic {

    private final RenderManager renderer;
    private final ObjectLoader loader;
    private final WindowManager window;

    private Entity entity;
    private Camera camera;

    Vector3f cameraInc;

    public TestGame() {
        renderer = new RenderManager();
        window = Launcher.getWindow();
        loader = new ObjectLoader();
        camera = new Camera();
        cameraInc = new Vector3f(0,0,0);
    }

    @Override
    public void init() throws Exception {
        renderer.init();

        Model model = loader.loadOBJModel("/models/monkey.obj");
        model.setTexture(new Texture(loader.loadTexture("textures/grassblock.png")));

        entity = new Entity(model, new Vector3f(0,0,-5), new Vector3f(0,0,0), 1);
    }

    @Override
    public void input() {
        // Reset cameraInc vector
        cameraInc.set(0, 0, 0);

        // Update cameraInc based on key inputs
        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            cameraInc.z = -1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            cameraInc.z = 1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            cameraInc.x = -1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_Q)) {
            cameraInc.y = -1;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_E)) {
            cameraInc.y = 1;
        }
    }


    @Override
    public void update(float interval, MouseInput mouseInput) {
        camera.movePosition(cameraInc.x * CAMERA_STEP, cameraInc.y * CAMERA_STEP, cameraInc.z * CAMERA_STEP);

        if(mouseInput.isRightButtonPress()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        entity.incRotation(0.0f, 0.5f,0.0f);

    }

    @Override
    public void render() {
        if(window.isResize()){
            GL11.glViewport(0,0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }

        window.setClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        renderer.render(entity, camera);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        loader.cleanup();
    }
}
