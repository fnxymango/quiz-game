import java.io.*;
import java.net.*;
import java.util.*;

public class QuizServer {

    private static final int PORT = 1234;
    private static final List<String> QUESTIONS = Arrays.asList(
        "What is 3x3?",  // 1번 질문
        "What year did the Korean War break?",  // 2번 질문
        "What is the largest mammal in the world?",  // 3번 질문
        "How many letters do Hangeul have?" ,
        "Who is the founder of the 'Apple'?"
    );
    private static final List<String> ANSWERS = Arrays.asList(
        "9",  // 1번 정답
        "1950",  // 2번 정답
        "blue whale",  // 3번 정답
        "35",
        "steve jobs"
    );

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");
            while (true) {
                try {
                    // 새로운 클라이언트가 연결되면 새로운 스레드를 생성하여 클라이언트를 처리
                    Socket clientSocket = serverSocket.accept();
                    new ClientHandler(clientSocket).start();  // ClientHandler는 스레드를 생성하여 클라이언트를 처리
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 클라이언트를 처리할 스레드
    private static class ClientHandler extends Thread {
        private final Socket clientSocket;

        // 생성자
        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                
                String clientMessage;
                int score = 0;
                int questionIndex = 0;

                // 클라이언트가 보내는 START_QUIZ 명령을 기다림
                clientMessage = reader.readLine();
                if (clientMessage == null) {
                    System.err.println("Client disconnected unexpectedly.");
                    return;  // 클라이언트가 예상치 못하게 연결을 끊은 경우
                }

                if ("START_QUIZ".equals(clientMessage)) {
                    while (questionIndex < QUESTIONS.size()) {
                        // 질문을 클라이언트로 전송
                        writer.println("QUESTION:" + (questionIndex + 1) + ":" + QUESTIONS.get(questionIndex));

                        // 클라이언트 답안 받기
                        clientMessage = reader.readLine();
                        if (clientMessage == null) {
                            System.err.println("Client disconnected unexpectedly during the quiz.");
                            return;  // 클라이언트가 중간에 연결을 끊은 경우
                        }

                        if (clientMessage.startsWith("ANSWER:")) {
                            String clientAnswer = clientMessage.split(":")[1].trim().toLowerCase();
                            String correctAnswer = ANSWERS.get(questionIndex);

                            // 정답 체크
                            if (clientAnswer.equals(correctAnswer)) {
                                writer.println("FEEDBACK:Correct!");
                                score++;  // 점수 증가
                            } else {
                                writer.println("FEEDBACK:Incorrect.");
                            }
                        }
                        questionIndex++;
                    }
                }

                // 최종 점수 전송
                writer.println("FINAL_SCORE:" + score + "/" + QUESTIONS.size());

            } catch (IOException e) {
                System.err.println("Error handling client communication: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();  // 클라이언트와의 연결 종료
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}