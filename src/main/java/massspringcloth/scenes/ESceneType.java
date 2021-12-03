package massspringcloth.scenes;

import renderengine.camera.ThirdPersonCamera;

/**
 * @author Mirco Werner
 */
public enum ESceneType {
    HANGING,
    HANGING_PLANE,
    SPHERE,
    FLAG;

    public static IScene createScene(ESceneType scene, ThirdPersonCamera camera) throws Exception {
        return switch (scene) {
            case HANGING -> new HangingScene(camera);
            case HANGING_PLANE -> new HangingPlaneScene(camera);
            case SPHERE -> new SphereScene(camera);
            case FLAG -> new FlagScene(camera);
        };
    }
}
