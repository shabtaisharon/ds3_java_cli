/*
 * ******************************************************************************
 *   Copyright 2014-2017 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ****************************************************************************
 */

package com.spectralogic.ds3cli.helpers;

import com.google.common.collect.ImmutableList;
import com.spectralogic.ds3cli.Arguments;
import com.spectralogic.ds3cli.CommandResponse;
import com.spectralogic.ds3cli.Ds3ProviderImpl;
import com.spectralogic.ds3cli.FileSystemProviderImpl;
import com.spectralogic.ds3cli.command.CliCommand;
import com.spectralogic.ds3cli.command.CliCommandFactory;
import com.spectralogic.ds3cli.util.Ds3Provider;
import com.spectralogic.ds3cli.util.FileSystemProvider;
import com.spectralogic.ds3cli.util.FileUtils;
import com.spectralogic.ds3client.Ds3Client;
import com.spectralogic.ds3client.commands.spectrads3.GetSystemInformationSpectraS3Request;
import com.spectralogic.ds3client.helpers.Ds3ClientHelpers;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

public final class Util {
    public static final String RESOURCE_BASE_NAME = "./src/test/resources/books/";
    public static final String DOWNLOAD_BASE_NAME = "output/";
    private static final int TRANSFER_RETRY_ATTEMPTS = 5;

    private Util() {
        //pass
    }


    public static CommandResponse command(final Ds3Client client, final Arguments args) throws Exception {
        final Ds3Provider provider = new Ds3ProviderImpl(client, Ds3ClientHelpers.wrap(client, TRANSFER_RETRY_ATTEMPTS));
        final FileSystemProvider fileSystemProvider = new FileSystemProviderImpl();
        final CliCommand command = CliCommandFactory.getCommandExecutor(args.getCommand()).withProvider(provider, fileSystemProvider);
        command.init(args);
        return command.render();
    }

    public static CommandResponse command(final Ds3Client client, final String commandLine) throws Exception {
        return command(client, new Arguments(commandLine.split(" ")));
    }

    public static CommandResponse getBucket(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "get_bucket", "-b", bucketName});
        return command(client, args);
    }

    public static CommandResponse getService(final Ds3Client client) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "get_service"});
        return command(client, args);
    }

    public static CommandResponse createBucket(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bucket", "-b", bucketName});
        return command(client, args);
    }

    public static CommandResponse deleteBucket(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "delete_bucket", "-b", bucketName, "--force"});
        return command(client, args);
    }

    public static CommandResponse putBulk(final Ds3Client client, final String bucketName, final String localDirectory) throws Exception {
        final Arguments args = new Arguments(
                new String[]{
                        "--http",
                        "-c", "put_bulk",
                        "-b", bucketName,
                        "-d", localDirectory,
                        "-nt", "3"});
        return command(client, args);
    }

    public static CommandResponse putPerformanceFiles(final Ds3Client client, final String bucketName, final int fileCount, final long fileSize) throws Exception {
        final Arguments args = new Arguments(
                new String[]{
                        "--http",
                        "-c", "performance",
                        "-b", bucketName,
                        "-n", Integer.toString(fileCount),
                        "-s", Long.toString(fileSize),
                        "--do-not-delete" });
        return Util.command(client, args);
    }

    public static CommandResponse getBulk(final Ds3Client client, final String bucketName, final String localDirectory) throws Exception {
        final Arguments args = new Arguments(
                new String[]{
                        "--http",
                        "-c", "get_bulk",
                        "-b", bucketName,
                        "-d", localDirectory,
                        "-nt", "3"});
        return command(client, args);
    }

    public static CommandResponse getJobs(final Ds3Client client) throws Exception {
        final Arguments args = new Arguments(
                new String[]{
                        "--http",
                        "-c", "get_jobs"});
        return command(client, args);
    }

    public static CommandResponse getCompletedJobs(final Ds3Client client) throws Exception {
        final Arguments args = new Arguments(
                new String[]{
                        "--http",
                        "-c", "get_jobs",
                        "--completed"});
        return command(client, args);
    }

    public static CommandResponse getObject(final Ds3Client client, final String bucketName, final String objectName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "get_object", "-b", bucketName, "-o", objectName});
        return command(client, args);
    }

    public static CommandResponse loadBookTestData(final Ds3Client client, final String bucketName) throws Exception {
        final Arguments args = new Arguments(new String[]{"--http", "-c", "put_bulk", "-b", bucketName, "-d", RESOURCE_BASE_NAME});
        return command(client, args);
    }

    public static void deleteLocalFile(final String fileName) throws IOException {
        Files.deleteIfExists(Paths.get(DOWNLOAD_BASE_NAME + fileName));
    }

    public static Iterable<String> readLocalFile(final String fileName) throws IOException {
        return Files.readAllLines(Paths.get(DOWNLOAD_BASE_NAME + fileName), Charset.forName("utf-8"));
    }

    public static void copyFile(final String fileName, final String from, final String to) throws IOException {
        final Path toDir = Paths.get(to);

        // will fail silently if the destination directory already exists
        Files.createDirectories(toDir);

        Files.copy(Paths.get(from + fileName), Paths.get(to + fileName), StandardCopyOption.REPLACE_EXISTING);
    }

    public static void deleteLocalFiles() throws IOException {
        final Iterable<Path> files = FileUtils.listObjectsForDirectory(Paths.get(DOWNLOAD_BASE_NAME));
        for (final Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    public static int countLocalFiles() throws IOException {
        final ImmutableList<Path>  files = FileUtils.listObjectsForDirectory(Paths.get(DOWNLOAD_BASE_NAME));
        return files.size();
    }

    public static double getBlackPearlVersion(final Ds3Client client) throws IOException {
        final String buildInfo = client.getSystemInformationSpectraS3(new GetSystemInformationSpectraS3Request()).getSystemInformationResult().getBuildInformation().getVersion();
        final String[] buildInfoArr = buildInfo.split((Pattern.quote(".")));
        return Double.valueOf(String.format("%s.%s", buildInfoArr[0], buildInfoArr[1]));
    }
}
