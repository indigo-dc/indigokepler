package pl.psnc.indigo.fg.kepler;

import pl.psnc.indigo.fg.api.restful.BaseAPI;
import pl.psnc.indigo.fg.api.restful.TasksAPI;
import pl.psnc.indigo.fg.api.restful.exceptions.FutureGatewayException;
import pl.psnc.indigo.fg.api.restful.jaxb.Task;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.SingletonAttribute;

import java.net.URISyntaxException;

public class GetTask extends LimitedFiringSource {
    public TypedIOPort userPort;
    public TypedIOPort idPort;
    public TypedIOPort statusPort;

    public GetTask(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        userPort = new TypedIOPort(this, "user", true, false);
        new SingletonAttribute(userPort, "_showName");
        userPort.setTypeEquals(BaseType.STRING);

        idPort = new TypedIOPort(this, "id", true, false);
        new SingletonAttribute(idPort, "_showName");
        idPort.setTypeEquals(BaseType.STRING);

        statusPort = new TypedIOPort(this, "status", false, true);
        new SingletonAttribute(statusPort, "_showName");
        statusPort.setTypeEquals(BaseType.STRING);

        output.setTypeEquals(BaseType.STRING);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String user = PortHelper.readString(userPort);
        String id = PortHelper.readString(idPort);

        Task task = new Task();
        task.setUser(user);
        task.setId(id);

        try {
            TasksAPI restAPI = new TasksAPI(BaseAPI.LOCALHOST_ADDRESS);
            task = restAPI.getTask(task);
            output.send(0, new StringToken(task.getId()));
            statusPort.send(0, new StringToken(task.getStatus().name()));
        } catch (FutureGatewayException | URISyntaxException e) {
            throw new IllegalActionException(this, e, "Failed to get task");
        }
    }
}
