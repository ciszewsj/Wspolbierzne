import math

import pygame
from pygame.locals import *

pygame.init()
clock = pygame.time.Clock()

# Define colors
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)

# Set up the display
WINDOW_SIZE = (400, 400)
screen = pygame.display.set_mode(WINDOW_SIZE)

# Define the vertices of the cube
cube_vertices = [
    (-50, -50, -50),
    (50, -50, -50),
    (50, 50, -50),
    (-50, 50, -50),
    (-50, -50, 50),
    (50, -50, 50),
    (50, 50, 50),
    (-50, 50, 50)
]

# Define the faces of the cube
cube_faces = [
    (0, 1, 2, 3),
    (1, 5, 6, 2),
    (5, 4, 7, 6),
    (4, 0, 3, 7),
    (0, 4, 5, 1),
    (3, 2, 6, 7)
]

# Define the camera position
camera_position = [0, 0, -500]

# Define the projection parameters
fov = 500
near_clip = 0.1
far_clip = 1000


def project(point):
    # Convert 3D point to 2D screen point
    x, y, z = point
    x -= camera_position[0]
    y -= camera_position[1]
    z -= camera_position[2]
    if z == 0:
        z = 0.1
    k = fov / z
    x = int(x * k + WINDOW_SIZE[0] / 2)
    y = int(-y * k + WINDOW_SIZE[1] / 2)
    return x, y


def draw_cube(cube_vertices, cube_faces):
    for face in cube_faces:
        point_list = []
        for i in face:
            point = cube_vertices[i]
            point = project(point)
            point_list.append(point)
        pygame.draw.polygon(screen, WHITE, point_list)


# Set the initial rotation angles
x_rotation = 0
y_rotation = 0

# Main loop
running = True
while running:
    for event in pygame.event.get():
        if event.type == QUIT:
            running = False

    # Rotate the cube
    x_rotation += 1
    y_rotation += 1

    # Clear the screen
    screen.fill(BLACK)

    # Rotate the vertices of the cube
    rotated_cube_vertices = []
    for vertex in cube_vertices:
        x, y, z = vertex

        # Rotate around X-axis
        y1 = y * math.cos(x_rotation) - z * math.sin(x_rotation)
        z1 = y * math.sin(x_rotation) + z * math.cos(x_rotation)

        # Rotate around Y-axis
        x2 = x * math.cos(y_rotation) - z1 * math.sin(y_rotation)
        z2 = x * math.sin(y_rotation) + z1 * math.cos(y_rotation)

        rotated_cube_vertices.append((x2, y1, z2))

    # Draw the rotated cube
    draw_cube(rotated_cube_vertices, cube_faces)

    pygame.display.flip()
    clock.tick(60)

pygame.quit()
