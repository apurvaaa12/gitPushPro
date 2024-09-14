package com.gitpushPro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GitpushProApplication {

	private static final Logger logger = LogManager.getLogger(GitpushProApplication.class);

	@Value("${git.local.repo.path}")
	private static String localRepoPath;

	@Value("${gitlab.username}")
	private static String userName;

	@Value("${gitlab.password}")
	private static String password;

	@Value("${gitlab.remote.url}")
	private static String url;



	public static void main(String[] args) throws GitAPIException, IOException {
		SpringApplication.run(GitpushProApplication.class, args);
		pushToGitLab();

	}


	private static void pushToGitLab() throws GitAPIException, IOException {

//		Authenticator.setDefault(new Authenticator() {
//			protected PasswordAuthentication getPasswordAuthentication() {
//				return new PasswordAuthentication(userName, password.toCharArray());
//			}
//		});

//		//Pulling the latest changes from git
//		pullFromGit();

		// Open the Git repository
		try (Git git = Git.open(new File("/Users/sreeapurva.rajasekharuni/Desktop/Apurva/gitPushPro"))) {

			// Get the configuration for the repository
			StoredConfig config = git.getRepository().getConfig();
			logger.info("Config: " +config);

			//Get the status of current branch that we're in
			String status = git.getRepository().getBranch();
			logger.info("Our current branch: " +status );

			// Add file to the repository
			git.add().addFilepattern(".").call();
			logger.info("Added Files to git");

			// Commit the changes
			git.commit().setMessage("Added CSV file").call();
			logger.info("Commit Successful!");

			// Push to the GitLab
			git.push()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider("apurvaaa12", "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"))
					.setRemote("https://github.com/apurvaaa12/gitPushPro.git")
					.call();
			logger.info("Pushed the code to git");

		} catch (IOException e) {
			logger.debug(e);
			logger.error("Failed to push to git" + e);
			throw new RuntimeException(e);
		}
	}

	/***
	 * iterating over the commit history and checking if we have any changes to pull
	 * @param git using Jgit.
	 * @throws GitAPIException in case of exception.
	 */
	private static boolean hasChanges(Git git) throws GitAPIException {
		Iterable<RevCommit> commits = git.log().call();
		return commits.iterator().hasNext();
	}

	/***
	 * If there are any changes in the current branch this method is pulling the changes to the local repository
	 */
	private static void pullFromGit() {
		try {
			Git git = Git.open(new File(localRepoPath));

			// Check if there are any changes
			if (!hasChanges(git)) {
				logger.info("No changes to pull.");
				return;
			}

			PullCommand pullCmd = git.pull();

			// Set credentials if needed
			if (userName != null && password != null) {
				pullCmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(userName, password));
			}

			// Pull changes
			pullCmd.call();

			logger.info("Pull successful repository up to date");
		} catch (IOException | GitAPIException e) {
			logger.debug(e);
			logger.error("Failed to pull changes");
		}
	}
}

