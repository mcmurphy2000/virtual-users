package com.atlassian.performance.tools.virtualusers.api

import com.atlassian.performance.tools.jirasoftwareactions.api.JiraSoftwareScenario
import com.atlassian.performance.tools.virtualusers.api.browsers.GoogleChrome
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserBehavior
import com.atlassian.performance.tools.virtualusers.api.config.VirtualUserTarget
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Test
import java.net.URI

class VirtualUserOptionsTest {

    private val optionsTemplate = VirtualUserOptions(
        target = VirtualUserTarget(
            webApplication = URI("http://localhost/jira/"),
            userName = "fred",
            password = "secret"
        ),
        behavior = VirtualUserBehavior.Builder(JiraSoftwareScenario::class.java)
            .load(VirtualUserLoad())
            .seed(352798235)
            .diagnosticsLimit(8)
            .browser(GoogleChrome::class.java)
            .skipSetup(true)
            .build()
    )

    @Test
    fun shouldConvertToCli() {
        val args = optionsTemplate.toCliArgs()

        assertThat(args)
            .containsSequence(
                "--jira-address",
                "http://localhost/jira/"
            )
            .containsSequence(
                "--login",
                "fred"
            )
            .containsSequence(
                "--password",
                "secret"
            )
            .containsSequence(
                "--scenario",
                "com.atlassian.performance.tools.jirasoftwareactions.api.JiraSoftwareScenario"
            )
            .containsSequence(
                "--seed",
                "352798235"
            )
            .containsSequence(
                "--diagnostics-limit",
                "8"
            )
            .containsSequence(
                "--hold",
                "PT0S"
            )
            .containsSequence(
                "--ramp",
                "PT15S"
            )
            .containsSequence(
                "--flat",
                "PT5M"
            )
            .containsSequence(
                "--browser",
                "com.atlassian.performance.tools.virtualusers.api.browsers.GoogleChrome"
            )
            .contains("--skip-setup")
    }

    @Test
    fun shouldParseItself() {
        val parser = VirtualUserOptions.Parser()

        val cliArgs = optionsTemplate.toCliArgs()
        val reparsedCliArgs = parser.parse(cliArgs).toCliArgs()

        assertThat(reparsedCliArgs).isEqualTo(cliArgs)
    }

    @Test
    fun shouldReturnSamePathIfValid() {
        val options = optionsTemplate.withJiraAddress(URI("http://localhost:8080/"))

        assertThat(options.toCliArgs()).contains("http://localhost:8080/")
    }

    @Test
    fun shouldAppendPathIfMissing() {
        val options = optionsTemplate.withJiraAddress(URI("http://localhost:8080"))

        assertThat(options.toCliArgs()).contains("http://localhost:8080/")
    }

    @Test
    fun shouldThrowOnInvalidUri() {
        val thrown = catchThrowable {
            optionsTemplate.withJiraAddress(URI("http://localhost:8080invalid"))
        }

        assertThat(thrown).hasMessageContaining("http://localhost:8080invalid")
    }

    @Test
    fun shouldFixDanglingContextPath() {
        val options = optionsTemplate.withJiraAddress(URI("http://localhost:8080/context-path"))

        assertThat(options.toCliArgs()).contains("http://localhost:8080/context-path/")
    }

    @Test
    fun shouldAllowInsecureConnections() {
        val options = optionsTemplate.withAllowInsecureConnections(true)

        assertThat(options.toCliArgs()).contains("--allow-insecure-connections")
    }

    private fun VirtualUserOptions.withJiraAddress(
        jiraAddress: URI
    ) = withTarget(
        VirtualUserTarget(
            webApplication = jiraAddress,
            userName = target.userName,
            password = target.password
        )
    )

    @Suppress("DEPRECATION")
    private fun VirtualUserOptions.withAllowInsecureConnections(
        allowInsecureConnections: Boolean
    ) = VirtualUserOptions(
        jiraAddress = jiraAddress,
        scenario = scenario,
        virtualUserLoad = virtualUserLoad,
        adminLogin = adminLogin,
        adminPassword = adminPassword,
        diagnosticsLimit = diagnosticsLimit,
        seed = seed,
        allowInsecureConnections = allowInsecureConnections,
        help = help
    )
}