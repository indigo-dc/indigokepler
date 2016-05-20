package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.InputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Link;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.util.ArrayList;
import java.util.List;

public class CreateTask extends LimitedFiringSource {
    public TypedIOPort userPort;            // user name in FG database
    public TypedIOPort applicationPort;     // application id in FG database
    public TypedIOPort descriptionPort;     // description passed to app
    public TypedIOPort argumentsPort;       // array of arguments passed to app
    public TypedIOPort inputFilesPort;      // array of input files
    public TypedIOPort outputFilesPort;     // array of output files
    public TypedIOPort inputLoctaion;

    public CreateTask(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        applicationPort = new TypedIOPort(this, "application", true, false);
        new SingletonAttribute(applicationPort, "_showName");
        applicationPort.setTypeEquals(BaseType.STRING);

        descriptionPort = new TypedIOPort(this, "description", true, false);
        new SingletonAttribute(descriptionPort, "_showName");
        descriptionPort.setTypeEquals(BaseType.STRING);

        argumentsPort = new TypedIOPort(this, "arguments", true, false);
        new SingletonAttribute(argumentsPort, "_showName");
        argumentsPort.setTypeEquals(BaseType.GENERAL);

        inputFilesPort = new TypedIOPort(this, "input_files", true, false);
        new SingletonAttribute(inputFilesPort, "_showName");
        inputFilesPort.setTypeEquals(BaseType.GENERAL);

        outputFilesPort = new TypedIOPort(this, "output_files", true, false);
        new SingletonAttribute(outputFilesPort, "_showName");
        outputFilesPort.setTypeEquals(BaseType.GENERAL);

        inputLoctaion = new TypedIOPort(this, "inputs", false, true);
        new SingletonAttribute(inputLoctaion, "_showName");
        inputLoctaion.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String userString = null;
        String applicationString = null;
        String descriptionString = null;

        if (userPort.getWidth() > 0) {
            StringToken userToken = (StringToken) userPort.get(0);
            userString = userToken.stringValue();
        }

        if (applicationPort.getWidth() > 0) {
            StringToken applicationToken = (StringToken) applicationPort.get(0);
            applicationString = applicationToken.stringValue();
        }

        if (descriptionPort.getWidth() > 0) {
            StringToken descriptionToken = (StringToken) descriptionPort.get(0);
            descriptionString = descriptionToken.stringValue();
        }

        List<String> argumentsArray = new ArrayList();

        if (argumentsPort.getWidth() > 0) {
            ArrayToken argumentsToken = (ArrayToken) argumentsPort.get(0);
            for (int i = 0; i < argumentsToken.length(); i++) {
                StringToken arrayElement = (StringToken) argumentsToken.getElement(i);
                argumentsArray.add(arrayElement.stringValue());
            }
        }

        List<InputFile> inputFilesArray = new ArrayList();

        if (inputFilesPort.getWidth() > 0) {
            ArrayToken inputFilesToken = (ArrayToken) inputFilesPort.get(0);
            for (int i = 0; i < inputFilesToken.length(); i++) {
                StringToken arrayElement = (StringToken) inputFilesToken.getElement(i);
                InputFile _tmpFile = new InputFile();
                _tmpFile.setName(arrayElement.stringValue());
                inputFilesArray.add(_tmpFile);
            }
        }

        List<OutputFile> outputFilesArray = new ArrayList();

        if (outputFilesPort.getWidth() > 0) {
            ArrayToken outputFilesToken = (ArrayToken) outputFilesPort.get(0);
            for (int i = 0; i < outputFilesToken.length(); i++) {
                StringToken arrayElement = (StringToken) outputFilesToken.getElement(i);
                OutputFile _tmpFile = new OutputFile();
                _tmpFile.setName(arrayElement.stringValue());
                outputFilesArray.add(_tmpFile);
            }
        }

        Task taskToCreate = new Task();
        taskToCreate.setUser(userString);
        taskToCreate.setDescription(descriptionString);
        taskToCreate.setApplication(applicationString);
        taskToCreate.setArguments(argumentsArray);
        taskToCreate.setInputFiles(inputFilesArray);
        taskToCreate.setOutputFiles(outputFilesArray);

        Task result;
        try {
            TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
            result = restAPI.createTask(taskToCreate);
        } catch (FutureGatewayException e) {
            throw new IllegalActionException(this, e, "Failed to create task");
        }

        // We have to make sure there are inputs required by the application
        String linksURL = "";
        for (Link l : result.getLinks()) {
            if (l.getRel().equals("input")) {
                linksURL = l.getHref();
            }
        }

        inputLoctaion.send(0, new StringToken(linksURL));
        output.send(0, new StringToken(result.getId()));
    }
}
