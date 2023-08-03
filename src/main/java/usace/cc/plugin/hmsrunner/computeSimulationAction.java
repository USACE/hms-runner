package usace.cc.plugin.hmsrunner;

import hms.model.Project;
import usace.cc.plugin.Action;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class computeSimulationAction {
    private Action action;
    private String simulationName;
    public computeSimulationAction(Action a, String simname) {
        this.action = a;
        this.simulationName = simname;
    }
    public void computeAction(){
        String hmsFilePath = action.getParameters().get("project_file").getPaths()[0];
        System.out.println("opening project " + hmsFilePath);
        Project project;
        try {
            project = Project.open(hmsFilePath);
        } catch (Exception e) {
            System.out.println("ERROR failed to open the " + hmsFilePath + " project.");
            return;
        }
        System.out.println("preparing to run Simulation " + simulationName);
        project.computeRun(simulationName);

        int lastDotIndex = hmsFilePath.lastIndexOf('.'); // Get index of the dot before hms, e.g model.hms
        String logFilePath = hmsFilePath.substring(0, lastDotIndex) + ".log";
        boolean simError = containsError(logFilePath);
        if(simError)
            System.out.println("ERROR running simulation for " + hmsFilePath);
        else
            System.out.println("SUCCESS running simulation for " + hmsFilePath);
        project.close();
    }

    private boolean containsError(String filepath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("ERROR")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
