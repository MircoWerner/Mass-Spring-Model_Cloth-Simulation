# Mass-Spring-Model_Cloth-Simulation

This program provides an interactive real time cloth simulation by using the mass-spring model. It is written in Java and uses LWJGL to access OpenGL. For performance reasons, the calculation of the model is done on the GPU in a compute shader allowing the simulation to use smaller time steps.

## Table of contents
1. [ Features ](#features)
2. [ System Requirements ](#system)
3. [ Build ](#build)
4. [ Controls ](#controls)
5. [ Information about the code and program flow ](#code)
6. [ Results ](#results)
7. [ References ](#references)

<a name="features"></a>
## Features
- Cloth simulated with the mass-spring model considering internal forces (springs) and external forces (gravity, viscous damping (friction), viscous interaction (fluid like mediums, wind))
- Relaxation to ensure length constraints of the joints (joints will not become disproportionately extended) and to make the model more stable
- Phong shading and normal mapping

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
   - Scene buttons: Switch or restart the selected scene. Left to right: hanging cloth (wind on/off button below, only enabled for this scene), cloth hanging in the xz-plane from 3 points, cloth falling on a sphere, flag in wind
2. Movement and Camera:
   - W,A,S,D,Shift,Space: Move the camera forward,left,backward,right,down,up.
   - Left mouse button (press and hold) and dragging the mouse: Rotate the camera.
   - Mouse wheel: Zoom the camera.
3. Other:
   - T (press and hold): Render cloth as wireframe (only edges of the mesh visible).

<img src="https://user-images.githubusercontent.com/34870366/144678843-c39cbd50-fc67-4586-b686-d569a004eb3c.png" width="80%" alt="img_gui">

<a name="code"></a>
## Information about the code
1. The package `src/main/java/massspringcloth/` contains all the classes directly related to the construction and simulation of the cloth model.
2. The package `src/main/java/renderengine/` contains the classes needed to communicate with and access OpenGL.
3. The resource folder `src/main/resources/shaders/` contains the vertex, fragment and compute shaders for the program. Especially:
   - `cloth_compute.glsl` is the compute shader where the main calculation of the new positions of the mass spring model happens.
   - `cloth_vert.glsl` and `cloth_frag.glsl` are the vertex and fragment shader to render the result.

A few words about the program flow:
1. When a `massspringcloth/scene/IScene` is created by the `massspringcloth/simulation/SimulationController.java` the initial positions, velocities and locked points are defined.
2. They are passed to the `massspringcloth/cloth/MassSpringModel.java` where the vertices, texture coordinates and indices are calculated. 
3. After that in the `massspringcloth/cloth/MassSpringCloth.java` the OpenGL buffers (vertex buffer objects (vbo)) for the vertex shader are created as well as the input and output buffers of the compute shader.
4. When the simulation starts the compute shader is executed multiple times in parallel for each point. It computes the new positions from the positions in the input buffer and writes the updated data to the output buffer as well as to the position vbo of the vertex shader. After each compute shader execution, the input and output buffer of the compute shader are swapped so that it gets the updated data as its new input. Normals and tangents are calculated in the compute shader as well for lighting and normal mapping. Depending on the settings, the compute shader either calculates the new positions due to acting internal and external forces or does relaxation (adjusting the positions of the points to bring two points closer to another) to prevent disproportional extented joints. For each iteration both compute shader "stages" are executed successively.
5. After the compute shader has run multiple times, the updated data in the vertex positions buffer is rendered by the vertex and fragment shader.
6. Step four and five repeat until the simulation is stopped or the scene is changed.

<a name="results"></a>
## Results
<img src="https://user-images.githubusercontent.com/34870366/144679330-c14c6606-0ea9-40f2-9801-22682659fcbb.png" width="40%" alt="img_cloth_hanging"><br />
Cloth hanging from two points.<br />
<img src="https://user-images.githubusercontent.com/34870366/144679398-dfa57396-d6e6-4cc1-9cbe-3a271bab47e8.png" width="40%" alt="img_cloth_hanging_wind"><br />
Cloth hanging from two points with wind.<br />
<img src="https://user-images.githubusercontent.com/34870366/144679406-39ef5893-ccb5-447d-857b-97ca1a5013c6.png" width="40%" alt="img_cloth_hanging_plane"><br />
Cloth hanging in the xz-plane from 3 points.<br />
<img src="https://user-images.githubusercontent.com/34870366/144679410-d069407c-3ec5-4885-b095-704c16afd07c.png" width="40%" alt="img_cloth_sphere"><br />
Cloth falling on a sphere.<br />
<img src="https://user-images.githubusercontent.com/34870366/144679413-db08a479-28f8-4561-9a03-a1849d71adaa.png" width="40%" alt="img_cloth_flag"><br />
Flag (cloth) with wind.

<a name="references"></a>
## References
*Main* OpenGL and LWJGL references that I have used:
- https://learnopengl.com/
- Tutorial series "OpenGL 3D Game Tutorial" by https://www.youtube.com/user/ThinMatrix
- https://www.lwjgl.org/guide
- https://github.com/LWJGL/lwjgl3-wiki/wiki/2.6.1.-Ray-tracing-with-OpenGL-Compute-Shaders-%28Part-I%29 (compute shaders in LWJGL)

Mass-Spring Model for cloth simulation reference:
- X. Provot, “Deformation constraints in a mass-spring model to describe rigid cloth behavior,” in IN GRAPHICS INTERFACE, 1995, pp. 147–154. [Online]. Available: https://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.84.1732
