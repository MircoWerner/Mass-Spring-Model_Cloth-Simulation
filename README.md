# Mass-Spring-Model_Cloth-Simulation

This program provides an interactive real time cloth simulation by using the mass-spring model. It is written in Java and uses LWJGL to access OpenGL. For performance reasons, the calculation of the model is done on the GPU in a compute shader allowing the simulation to use smaller time steps.

## Table of contents
1. [ System Requirements ](#system)
2. [ Build ](#build)
3. [ Controls ](#controls)
4. [ Information about the code and program flow ](#code)
5. [ Results ](#results)
6. [ References ](#references)

<a name="system"></a>
## System Requirements
- Java Version 14 or higher (otherwise small rewrites are necessary to undo new language features)
- OpenGL 4.3 or higher (support for compute shaders)
- Maven

I have tested the program on a Linux system. In the maven configuration file (pom.xml) Windows and MacOS are configured as well and might work.

<a name="build"></a>
## Build
1. Compile with `mvn compile`.
2. Execute with `mvn exec:java -Dexec.mainClass="massspringcloth.MassSpringClothRender"`.

(Or use a Java IDE with Maven support...)

<a name="controls"></a>
## Controls
1. User Interface (top left corner):
   - Play/Pause button: Start or stop the simulation.
   - Scene buttons: Switch or restart the selected scene. Left to right: hanging cloth, cloth falling on a sphere, flag in the wind (last one is not supported yet)
2. Movement and Camera:
   - W,A,S,D: Move the camera.
   - Left mouse button (press and hold) and dragging the mouse: Rotate the camera.
   - Mouse wheel: Zoom the camera.
3. Other:
   - T (press and hold): Render cloth as wireframe (only edges of the mesh visible).
 
<img src="https://user-images.githubusercontent.com/34870366/144140062-ba0757bc-cc5a-4d56-a7a8-29e3c257cb4e.png" width="80%" alt="img_cloth_sphere">

<a name="code"></a>
## Information about the code
1. The package `src/main/java/massspringcloth/` contains all the classes directly related to the construction and simulation of the cloth model.
2. The package `src/main/java/renderengine` contains the classes needed to communicate with and access OpenGL.
3. The resource folder `src/main/resources/shaders` contains the vertex, fragment and compute shaders for the program. Especially:
   - `cloth_compute.glsl` is the compute shader where the main calculation of the new positions of the mass spring model happens.
   - `cloth_vert.glsl` and `cloth_frag.glsl` are the vertex and fragment shader to render the result.

A few words to the program flow:
1. When a scene is created by the `massspringcloth/simulation/SimulationController.java` the initial positions, velocities and locked points are defined.
2. They are passed to the `massspringcloth/cloth/MassSpringModel.java` where the vertices, texture coordinates and indices are calculated. 
3. After that in the `massspringcloth/cloth/MassSpringCloth.java` the OpenGL buffers (vertex buffer objects (vbo)) for the vertex shader are created as well as the two buffers for the compute shader.
4. When the simulation starts the compute shader is executed multiple times in parallel for each pixel. It computes the new positions from the positions in the input buffer and writes the updated data to the output buffer as well as to the position vbo of the vertex shader. After each compute shader execution, the input and output buffer of the compute shader is changed so that it gets the updated data as its new input.
5. After the compute shader has run multiple times, the updated data in the vertex positions buffer is rendered by the vertex and fragment shader.
6. Step four and five repeat until the simulation is stopped or the scene is changed.

<a name="results"></a>
## Results
<img src="https://user-images.githubusercontent.com/34870366/144142676-0cd400ee-650d-4266-85e4-329bd2d55f98.png" width="30%" alt="img_cloth_hanging">
<img src="https://user-images.githubusercontent.com/34870366/144142687-f8a36c64-6b0c-42b3-82a1-5c43f19f8c7b.png" width="30%" alt="img_cloth_sphere">

<a name="references"></a>
## References
*Main* OpenGL and LWJGL references that I have used:
- https://learnopengl.com/
- Tutorial series "OpenGL 3D Game Tutorial" by https://www.youtube.com/user/ThinMatrix
- https://www.lwjgl.org/guide
- https://github.com/LWJGL/lwjgl3-wiki/wiki/2.6.1.-Ray-tracing-with-OpenGL-Compute-Shaders-%28Part-I%29 (compute shaders in LWJGL)

Mass-Spring Model for cloth simulation reference:
- X. Provot, “Deformation constraints in a mass-spring model to describe rigid cloth behavior,” in IN GRAPHICS INTERFACE, 1995, pp. 147–154. [Online]. Available: https://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.84.1732
