/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.runtime.entrypoint;

import org.apache.flink.runtime.entrypoint.parser.CommandLineOptions;
import org.apache.flink.runtime.entrypoint.parser.ParserResultFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import javax.annotation.Nonnull;

import java.util.Properties;

import static org.apache.flink.runtime.entrypoint.parser.CommandLineOptions.CONFIG_DIR_OPTION;
import static org.apache.flink.runtime.entrypoint.parser.CommandLineOptions.DYNAMIC_PROPERTY_OPTION;
import static org.apache.flink.runtime.entrypoint.parser.CommandLineOptions.HOST_OPTION;

public class EntrypointJobMasterConfigurationParserFactory
        implements ParserResultFactory<StandaloneJobGraphJobMasterConfiguration> {

    private static final Option JOB_GRAPH_FILE =
            Option.builder("jgraph")
                    .longOpt("job-graph")
                    .required(true)
                    .hasArg(true)
                    .argName("job graph file")
                    .desc("File path to the job-graph")
                    .build();

    public EntrypointJobMasterConfigurationParserFactory() {}

    @Override
    public Options getOptions() {
        final Options options = new Options();
        options.addOption(CONFIG_DIR_OPTION);
        options.addOption(DYNAMIC_PROPERTY_OPTION);
        options.addOption(HOST_OPTION);
        options.addOption(JOB_GRAPH_FILE);
        return options;
    }

    @Override
    public StandaloneJobGraphJobMasterConfiguration createResult(@Nonnull CommandLine commandLine)
            throws FlinkParseException {
        String jobGraphFile = commandLine.getOptionValue(JOB_GRAPH_FILE.getOpt());
        String flinkConfDir =
                commandLine.getOptionValue(CommandLineOptions.CONFIG_DIR_OPTION.getOpt());
        String hostname = commandLine.getOptionValue(CommandLineOptions.HOST_OPTION.getOpt());
        Properties dynamicProperties =
                commandLine.getOptionProperties(
                        CommandLineOptions.DYNAMIC_PROPERTY_OPTION.getOpt());
        return new StandaloneJobGraphJobMasterConfiguration(
                flinkConfDir,
                dynamicProperties,
                commandLine.getArgs(),
                hostname,
                this.getRestPort(commandLine),
                jobGraphFile);
    }

    private int getRestPort(CommandLine commandLine) throws FlinkParseException {
        String restPortString =
                commandLine.getOptionValue(CommandLineOptions.REST_PORT_OPTION.getOpt(), "-1");
        try {
            return Integer.parseInt(restPortString);
        } catch (NumberFormatException exp) {
            throw new FlinkParseException(
                    String.format(
                            "Failed to parse '--%s' option",
                            CommandLineOptions.REST_PORT_OPTION.getLongOpt()),
                    exp);
        }
    }
}
