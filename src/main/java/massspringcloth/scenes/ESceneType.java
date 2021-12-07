package massspringcloth.scenes;

import renderengine.camera.ThirdPersonCamera;

/**
 * Enumeration of all scenes in the program.
 *
 * @author Mirco Werner
 */
public enum ESceneType {
    HANGING,
    HANGING_PLANE,
    SPHERE,
    FLAG;

    /**
     * Creates the scene (object).
     *
     * @param scene  scene type to construct
     * @param camera camera of the scene
     * @return created scene (object)
     * @throws Exception if the creation fails
     */
    public static IScene createScene(ESceneType scene, ThirdPersonCamera camera) throws Exception {
        return switch (scene) {
            case HANGING -> new HangingScene(camera);
            case HANGING_PLANE -> new HangingPlaneScene(camera);
            case SPHERE -> new SphereScene(camera);
            case FLAG -> new FlagScene(camera);
        };
    }
}
