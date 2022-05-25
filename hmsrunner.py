from hms.model.JythonHms import *
import sys

print("attempt to run hms")
f = open("/workspaces/hms-runner/example_data/payload.yml", "r")
f.readline()
f.readline()
f.readline()
modelline = f.readline()
modelparts = modelline.split(': ')
modelName = modelparts[1]
print("model name is " + modelName)
f.readline()
path = f.readline().split(': ')[1]
project = f.readline().split(': ')[1].split(".")[0]
OpenProject(project, path)
Compute(modelName)
print("compute complete")
