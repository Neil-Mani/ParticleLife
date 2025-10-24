<img src="images/Particle Life.png" alt="Project Logo">

Particle life simulations are very simple because they only follow one rule which is the gravity equation but can result in countless different variations and shapes. The most base version of this project can be seen at <a href="neil-mani.github.io/ParticleLife">neil-mani.github.io/ParticleLife</a> .However, if you want a more advanced verion, you should use the app. Many things such as the worms are ONLY possible in the app because you can set specific gravitational forces for each particle.

To use the app, you are going to need some way to run java code until I am able to get a .exe file available. Any code editor would work. When you open the app, you will see a couple of different sections and a random seed for the simiulation that includes 2000 particles of all possible colors. The first (top section) is some general controls like hiding UI, clearing the canvas of all particles, and pausing the simulation. The randomize everything button can be used to randomize the whole simulation including the matrix (I will explain the matrix is shortly), the amount of particles on the canvas, and the colors used. Lastly, the randomize matrix just simply changes around the matrix.

The matrix is the magic of the whole simulation. It will allow you to change how particles interact with one another. Looking at the matrix, the colors on the side are indicating that that color particle is being affected by the interaction of the correlating particle of the box you changing values in. The values work pretty simply. 1 = attract and -1 = repel. You can use any 2 decimal number between these two to create slighter forces. Changing these values creates the uniqe shapes that you can see.

Lastly, you can manually add particles in the shape of a square, circle, ring, and line with a set number of particles per the add.

The app was made in java using swing. These 2 options were not the best but what was available. Because of this, simulating a very high particle number may cause lag. Also, there are many issues with the app. Looking at it now, the colors are mixed up where the matrix has one color but the actual simulation has a different color. These will be fixed with time.

🙂

Updates:

- On release, the matrix had a dark blue color which has now been removed and replaced by the actual particle color orange.
- Wrap, closed, and infinite canvas environments have been added.
- New optimizations have been added. The system supports up to 50,000 particles.
  For a good computer the frames look something like this:

  5,000 particles: Should run at 60 FPS easily
  10,000 particles: 50-60 FPS on most systems
  20,000 particles: 30-50 FPS 
  30,000+ particles: 20-40 FPS depending on your CPU
