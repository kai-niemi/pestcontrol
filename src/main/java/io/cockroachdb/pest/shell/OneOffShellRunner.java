package io.cockroachdb.pest.shell;

import org.springframework.core.annotation.Order;
import org.springframework.shell.Input;
import org.springframework.shell.Shell;
import org.springframework.shell.ShellRunner;
import org.springframework.shell.context.ShellContext;
import org.springframework.shell.jline.InteractiveShellRunner;
import org.springframework.shell.jline.NonInteractiveShellRunner;
import org.springframework.util.ObjectUtils;

import static io.cockroachdb.pest.shell.OneOffShellRunner.PRECEDENCE;

/**
 * A {@link ShellRunner} that looks for any arguments, which are then interpreted as
 * commands to run in headless mode and then exit.
 *
 * <p>Has higher precedence than all other shell runners which gives it
 * top priority to run the shell if arguments are found.
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
        if (ObjectUtils.isEmpty(args)) {
            return false;
        }

        super.run(args);

        shell.run(() -> () -> "quit");

        return true;
    }
}
