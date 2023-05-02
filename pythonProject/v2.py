import pygame
from pygame.locals import *
import numpy as np

# Define the colors
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
RED = (255, 0, 0)
GREEN = (0, 255, 0)
BLUE = (0, 0, 255)

# Initialize pygame
pygame.init()

# Set up the display
width, height = 800, 600
screen = pygame.display.set_mode((width, height))

# Set the caption of the window
pygame.display.set_caption("3D Cube")

# Define the cube vertices and faces
cube_vertices = np.array([
    [-1, -1, -1],
    [1, -1, -1],
    [1, 1, -1],
    [-1, 1, -1],
    [-1, -1, 1],
    [1, -1, 1],
    [1, 1, 1],
    [-1, 1, 1]
])

cube_faces = np.array([
    [0, 1, 2, 3],
    [1, 5, 6, 2],
    [5, 4, 7, 6],
    [4, 0, 3, 7],
    [3, 2, 6, 7],
    [4, 5, 1, 0]
])

# Define the camera position and projection parameters
camera_pos = np.array([0, 0, -5])
fov = 90
aspect_ratio = width / height
near_clip = 0.1
far_clip = 100

# Define the projection matrix
projection_matrix = np.array([
    [1 / (aspect_ratio * np.tan(np.deg2rad(fov / 2))), 0, 0, 0],
    [0, 1 / np.tan(np.deg2rad(fov / 2)), 0, 0],
    [0, 0, (far_clip + near_clip) / (far_clip - near_clip), 1],
    [0, 0, -(2 * far_clip * near_clip) / (far_clip - near_clip), 0]
])


# Define a function to project 3D points to 2D screen coordinates
def project(point):
    point = np.append(point, 1)
    projected = np.dot(projection_matrix, point)
    projected = projected / projected[3]
    x = (projected[0] + 1) * width / 2
    y = height - (projected[1] + 1) * height / 2
    return x, y


# Define a function to draw the cube
def draw_cube(vertices, faces):
    for face in faces:
        points = [project(vertices[i]) for i in face]
        pygame.draw.polygon(screen, WHITE, points, 1)


# Define the main loop
clock = pygame.time.Clock()
while True:
    for event in pygame.event.get():
        if event.type == QUIT:
            pygame.quit()
            quit()

    # Clear the screen
    screen.fill(BLACK)

    # Define the rotation matrices
    x_rotation = np.radians(pygame.time.get_ticks() / 10)
    y_rotation = np.radians(pygame.time.get_ticks() / 20)
    z_rotation = np.radians(pygame.time.get_ticks() / 30)
    x_rotation_matrix = np.array([
        [1, 0, 0],
        [0, np.cos(x_rotation), -np.sin(x_rotation)],
        [0, np.sin(x_rotation), np.cos(x_rotation)]
    ])
    y_rotation_matrix = np.array([
        [np.cos(y_rotation), 0, np.sin(y_rotation)],
        [0, 1, 0],
        [-np.sin(y_rotation), 0, np.cos(y_rotation)]
    ])
    z_rotation_matrix = np.array([
        [np.cos(z_rotation), -np.sin(z_rotation), 0],
        [np.sin(z_rotation), np.cos(z_rotation), 0],
        [0, 0, 1]
    ])

    # Apply the rotations to the cube vertices
    rotated_vertices = np.dot(cube_vertices, np.dot(x_rotation_matrix, np.dot(y_rotation_matrix, z_rotation_matrix)))

    # Draw the cube
    draw_cube(rotated_vertices, cube_faces)

    # Update the display
    pygame.display.flip()

    # Set the maximum frame rate
    clock.tick(1000)
