package massspringcloth.scenes;

import renderengine.camera.ThirdPersonCamera;

/**
 * @author Mirco Werner
 */
public enum ESceneType {
    HANGING,
    FLAG,
    SPHERE;

    public static IScene createScene(ESceneType scene, ThirdPersonCamera camera) throws Exception {
        return switch (scene) {
            case HANGING -> new HangingScene(camera);
            case SPHERE -> new SphereScene(camera);
            default -> null;
        };
    }
}
