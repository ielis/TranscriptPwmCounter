package org.monarchinitiative.pwm_transcript_annotator.cli.cmd;

import net.sourceforge.argparse4j.inf.Namespace;

public abstract class Command {

    public abstract void run(Namespace namespace) throws CommandException;
}
