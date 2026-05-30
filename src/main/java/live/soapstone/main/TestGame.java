package live.soapstone.main;

import live.soapstone.core.*;
import live.soapstone.core.entity.Entity;
import live.soapstone.core.entity.Model;
import live.soapstone.core.entity.Texture;
import live.soapstone.core.lighting.DirectionalLight;
import live.soapstone.core.lighting.PointLight;
import live.soapstone.core.lighting.SpotLight;
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

    private float lightAngle;
    private DirectionalLight directionalLight;
    private PointLight pointLight;
    private SpotLight spotLight;


    public TestGame() {
        renderer = new RenderManager();
        window = Launcher.getWindow();
        loader = new ObjectLoader();
        camera = new Camera();
        cameraInc = new Vector3f(0,0,0);
        lightAngle = -90;
    }

    @Override
    public void init() throws Exception {
        renderer.init();

        Model model = loader.loadOBJModel("/models/monkey.obj");
        model.setTexture(new Texture(loader.loadTexture("textures/grassblock.png")), 1f);
        entity = new Entity(model, new Vector3f(0,0,-5), new Vector3f(0,0,0), 1);

        //Point Light
        float lightIntensity = 2.0f;
        Vector3f lightPosition = new Vector3f(0, 0, -3.2f);
        Vector3f lightColor = new Vector3f(1, 1, 1);
        pointLight = new PointLight(lightColor, lightPosition, lightIntensity, 1, 1, 1);


        //Spot Light
        Vector3f coneDirection = new Vector3f(0, 0, 1);
        float cutoff = (float) Math.cos(Math.toRadians(180));
        spotLight = new SpotLight(new PointLight(lightColor, new Vector3f(0,0,1f), lightIntensity, 0, 0, 1), coneDirection, cutoff);

        //Directional Light
        lightPosition = new Vector3f(-1, -10, 0);
        lightColor = new Vector3f(1, 1, 1);
        directionalLight = new DirectionalLight(lightColor, lightPosition, lightIntensity);
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

        final float LIGHT_STEP = 0.0001f; // really small step because we don't have delta time yet

        if (window.isKeyPressed(GLFW.GLFW_KEY_O)) {
            pointLight.getPosition().x += LIGHT_STEP;
//            System.out.println("PointLight pos: " + pointLight.getPosition());
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_P)) {
            pointLight.getPosition().x -= LIGHT_STEP;
//            System.out.println("PointLight pos: " + pointLight.getPosition());
        }

        float lightPos = spotLight.getPointLight().getPosition().z;
        if(window.isKeyPressed(GLFW.GLFW_KEY_N))
            spotLight.getPointLight().getPosition().z = lightPos + 0.1f;
        if(window.isKeyPressed(GLFW.GLFW_KEY_M))
            spotLight.getPointLight().getPosition().z = lightPos - 0.1f;
    }



    @Override
    public void update(float interval, MouseInput mouseInput) {
//        float cameraSpeed = CAMERA_STEP * interval;
        camera.movePosition(cameraInc.x * CAMERA_STEP, cameraInc.y * CAMERA_STEP, cameraInc.z * CAMERA_STEP);

        if(mouseInput.isRightButtonPress()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        entity.incRotation(0.0f, 0.25f,0.0f);
        lightAngle += 0.5f;
        if (lightAngle > 90) {
            directionalLight.setIntensity(0);
            if (lightAngle >= 360)
                lightAngle = -90;
        } else if (lightAngle <= -80 || lightAngle >= 80) {
            float factor = 1 - (Math.abs(lightAngle)-80) / 10f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }
        double angleRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angleRad);
        directionalLight.getDirection().y = (float) Math.cos(angleRad);
    }

    @Override
    public void render() {
        if(window.isResize()){
            GL11.glViewport(0,0, window.getWidth(), window.getHeight());
            window.setResize(true);
        }

        renderer.render(entity, camera, directionalLight, pointLight, spotLight);
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        loader.cleanup();
    }
}
