package com.ai.assistant.real.estate.chat.interfaces.cli;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Component
@Profile("cli")
public class ChatCommandLineRunner implements CommandLineRunner {

    private static final String CONVERSATION_ID = "cli-session";

    private final ChatClient assistant;

    public ChatCommandLineRunner(ChatClient assistant) {
        this.assistant = assistant;
    }

    @Override
    public void run(String... args) {
        System.out.println("==============================================");
        System.out.println("  AI Real Estate Assistant — CLI Mode");
        System.out.println("  Type your question and press Enter.");
        System.out.println("  Type 'exit' or 'quit' to stop.");
        System.out.println("==============================================\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine().trim();

                if (input.isBlank()) {
                    continue;
                }
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                System.out.print("Assistant: ");
                String response = assistant.prompt(input)
                        .advisors(a -> a.param("conversationId", CONVERSATION_ID))
                        .call()
                        .content();
                System.out.println(response);
                System.out.println();
            }
        }
    }
}
