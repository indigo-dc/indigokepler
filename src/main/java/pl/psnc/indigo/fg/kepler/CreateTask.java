package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.InputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.OutputFile;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.AllowedPublicField;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * An actor which submits a new task using Future Gateway. See:
 * {@link TasksAPI#createTask(Task)}
 */
public class CreateTask extends FutureGatewayActor {
    /**
     * User name (mandatory).
     */
    private final TypedIOPort userPort;
    /**
     * Application id (mandatory).
     */
    private final TypedIOPort applicationPort;
    /**
     * Description of the task (optional).
     */
    private final TypedIOPort descriptionPort;
    /**
     * Arguments of the task (optional).
     */
    private final TypedIOPort argumentsPort;
    /**
     * Input files' paths (optional).
     */
    private final TypedIOPort inputFilesPort;
    /**
     * Output files' names (optional).
     */
    private final TypedIOPort outputFilesPort;

    public CreateTask(final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false); //NON-NLS
        userPort.setTypeEquals(BaseType.STRING);

        applicationPort =
                new TypedIOPort(this, "application", true, false); //NON-NLS
        applicationPort.setTypeEquals(BaseType.STRING);

        descriptionPort =
                new TypedIOPort(this, "description", true, false); //NON-NLS
        descriptionPort.setTypeEquals(BaseType.STRING);

        argumentsPort =
                new TypedIOPort(this, "arguments", true, false); //NON-NLS
        argumentsPort.setTypeEquals(new ArrayType(BaseType.STRING));

        inputFilesPort =
                new TypedIOPort(this, "input_files", true, false); //NON-NLS
        inputFilesPort.setTypeEquals(new ArrayType(BaseType.STRING));

        outputFilesPort =
                new TypedIOPort(this, "output_files", true, false); //NON-NLS
        outputFilesPort.setTypeEquals(new ArrayType(BaseType.STRING));

        output.setTypeEquals(BaseType.STRING);

        PortHelper
                .makePortNameVisible(userPort, applicationPort, descriptionPort,
                                     argumentsPort, inputFilesPort,
                                     outputFilesPort, output);
    }

    @Override
    public final void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readStringMandatory(userPort);
        String application = PortHelper.readStringMandatory(applicationPort);
        String description = PortHelper.readStringOptional(descriptionPort);
        List<String> arguments =
                PortHelper.readStringArrayOptional(argumentsPort);
        List<String> inputFileNames =
                PortHelper.readStringArrayOptional(inputFilesPort);
        List<String> outputFileNames =
                PortHelper.readStringArrayOptional(outputFilesPort);

        int inputSize = inputFileNames.size();
        int outputSize = outputFileNames.size();

        List<InputFile> inputFiles = new ArrayList<>(inputSize);
        for (final String fileName : inputFileNames) {
            InputFile inputFile = new InputFile();
            inputFile.setName(fileName);
            inputFiles.add(inputFile);
        }

        List<OutputFile> outputFiles = new ArrayList<>(outputSize);
        for (final String fileName : outputFileNames) {
            OutputFile outputFile = new OutputFile();
            outputFile.setName(fileName);
            outputFiles.add(outputFile);
        }

        Task task = new Task();
        task.setUser(user);
        task.setDescription(description);
        task.setApplication(application);
        task.setArguments(arguments);
        task.setInputFiles(inputFiles);
        task.setOutputFiles(outputFiles);

        try {
            String uri = getFutureGatewayUri();
            String token = getAuthorizationToken();
            TasksAPI api = new TasksAPI(URI.create(uri), token);

            task = api.createTask(task);
            String id = task.getId();
            output.send(0, new StringToken(id));
        } catch (final FutureGatewayException e) {
            throw new IllegalActionException(this, e, Messages.getString(
                    "failed.to.create.task"));
        }
    }
}
