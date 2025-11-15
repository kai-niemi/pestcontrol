package io.cockroachdb.pest.shell.support;

import org.springframework.core.annotation.Order;
import org.springframework.shell.Shell;
import org.springframework.shell.ShellRunner;
import org.springframework.shell.context.ShellContext;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.NonInteractiveShellRunner;
import org.springframework.util.ObjectUtils;

import static io.cockroachdb.pest.shell.support.OneOffShellRunner.PRECEDENCE;

/**
 * A {@link ShellRunner} that looks for any arguments, which are then interpreted as
 * commands to run in non-interactive mode and exit.
 *
 * <p>Has higher precedence than the {@link InteractiveShellRunner} and {@link NonInteractiveShellRunner}
 * which gives it priority to run the shell if at least one argument is found and it's not
 * captured by {@link org.springframework.shell.jline.ScriptShellRunner}
 *
 * @author Kai Niemi.
 */
@Order(PRECEDENCE)
public class OneOffShellRunner extends NonInteractiveShellRunner {
    /**
     * The precedence at which this runner is ordered by the DefaultApplicationRunner - which also controls
     * the order it is consulted on the ability to handle the current shell.
     */
    public static final int PRECEDENCE = InteractiveShellRunner.PRECEDENCE - 200;

    private final Shell shell;

    public OneOffShellRunner(Shell shell, ShellContext shellContext) {
        super(shell, shellContext);
        this.shell = shell;
    }

    @Override
    public boolean run(String[] args) throws Exception {
        String[] sourceArgs = args;
        if (ObjectUtils.isEmpty(sourceArgs)) {
            return false;
        }

        if (!sourceArgs[0].isEmpty() && sourceArgs[0].startsWith("@")) {
            return false;
        }

        super.run(sourceArgs);

        shell.run(() -> () -> "quit");

        return true;
    }
}
