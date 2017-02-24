package pl.psnc.indigo.fg.kepler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import pl.psnc.indigo.fg.kepler.helper.Messages;
import pl.psnc.indigo.fg.kepler.helper.PortHelper;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.LimitedFiringSource;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;

/**
 * Actor which downloads a named resource from a GitHub repository.
 */
@Slf4j
public class GitHubDownloadResource extends LimitedFiringSource {
    private final TypedIOPort repositoryPort;
    private final TypedIOPort branchTagPort;
    private final TypedIOPort remotePathPort;
    private final TypedIOPort outputFilePort;

    public GitHubDownloadResource(
            final CompositeEntity container, final String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        repositoryPort = new TypedIOPort(this, "repository", true, false);
        repositoryPort.setTypeEquals(BaseType.STRING);
        branchTagPort = new TypedIOPort(this, "branchTag", true, false);
        branchTagPort.setTypeEquals(BaseType.STRING);
        remotePathPort = new TypedIOPort(this, "remotePath", true, false);
        remotePathPort.setTypeEquals(BaseType.STRING);
        outputFilePort = new TypedIOPort(this, "outputFile", true, false);
        outputFilePort.setTypeEquals(BaseType.STRING);
        output.setTypeEquals(BaseType.BOOLEAN);

        PortHelper.makePortNameVisible(repositoryPort, branchTagPort,
                                       remotePathPort, outputFilePort);
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        String repositoryName = PortHelper.readStringMandatory(repositoryPort);
        String branchTag = PortHelper.readStringMandatory(branchTagPort);
        String remotePath = PortHelper.readStringMandatory(remotePathPort);
        File outputFile =
                new File(PortHelper.readStringMandatory(outputFilePort));

        URI uri = UriBuilder.fromUri("https://raw.githubusercontent.com")
                            .path(repositoryName).path(branchTag)
                            .path(remotePath).build();
        GitHubDownloadResource.log.debug("Attempting to download from {}", uri);

        try (InputStream stream = uri.toURL().openStream()) {
            FileUtils.copyInputStreamToFile(stream, outputFile);
        } catch (final IOException e) {
            throw new IllegalActionException(this, e, MessageFormat
                    .format(Messages.getString(
                            "failed.to.download.github.resource"),
                            repositoryName, branchTag, remotePath));
        }

        output.broadcast(new BooleanToken(true));
    }
}
