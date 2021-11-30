package massspringcloth;

import renderengine.engine.IRenderLogic;
import renderengine.engine.RenderEngine;

/**
 * @author Mirco Werner
 */
public class MassSpringClothRender {
    public static void main(String[] args) {
        try {
            IRenderLogic renderLogic = new MassSpringClothRenderLogic();
            RenderEngine renderEngine = new RenderEngine("Mass-Spring-Model Cloth-Simulation", 1280, 720, renderLogic);
            renderEngine.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
