# Java-OpenGL
Little 3D engine made using Java and OpenGL through LWJGL.

Based on this book, with my own additions:

https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/chapter1/chapter1.html

The LWJGL (Lightweight Java Game Library, https://www.lwjgl.org/) version is 3, so the engine uses modern OpenGL(3.0+, using vertex and fragment shaders).

Some of the features of the engine are:

-OBJ format support, although MTL is not yet supported which means textures are to be supplied in a separate single png file
-Types of lighting:
  - Directional light - sumilates sunlight, with dawns and dusks giving a slight red glow
  - Point light
  - Spot light
  - Ambient light
-Sky box support
-Terrain generation through heightmaps(png format)
-Terrain collision detection
-Mesh bounding box generation
-Mouse picker:
  -able to select 3D objects in world space through ray casting
   -every model has it's own ID and can be accessed at all times
  -able to get the screen cursor position onto the terrain
-Dynamic scene composition, i.e. different models(some with lights) can be added by the user at all times
-Smooth model moving from position A to position B
-HUD
  -Text rendered onto the screen, cyrillic and latin scripts are supported, prints selected object ID
  -Standard HUD
   - Custom made icons can be added in the top and bottom rows and right and left columns
   - Dynamic menu which at different times can consist of different buttons(icons)
   - Each button has it's own event key which can be accessed through the mouse picker upon being selected and can be used to trigger any wanted event. Some examples are buttons which control the sun speed and whether the sun is active or not, build button which upon selection shows a series of buttons which correspond to the models loaded upon initializing and can be placed anywhere on the terrain


Keep in mind that this is just a test project which I made in order to further understand Java and OOP in general, so the performance, graphics, and features of the engine may not be up to par with some of the more sophisticated engines out there. Also, this is a work in progress so I will be improving the engine and adding more features to it, whenever possible.

You can see a video showing the engine at work here:

https://www.youtube.com/watch?v=QZVZoizJHdM

 *Please note that the performance is slower than usual due to my old computer(nearly 10 years) which doesn't handle screen recording  software that well
 
 
If you see something useful in my code feel free to use it anyway you like.

Also, I highly recommend reading the book mentiond in the beginnig if you would like to learn more about Java and modern OpenGL.
   
