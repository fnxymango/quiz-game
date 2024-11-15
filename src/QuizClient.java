import java.io.*;
import java.net.*;

public class QuizClient {

    private static final String CONFIG_FILE = "server_info.dat";  // 서버 설정 파일 경로
    private static String serverIP = "localhost";  // 기본 서버 IP
    private static int serverPort = 1234;  // 기본 서버 포트

    public static void main(String[] args) {
        loadServerConfig();  // 서버 설정 파일을 읽어서 서버 IP와 포트를 로드함

        try (Socket socket = new Socket(serverIP, serverPort);  // 서버와 연결
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            // 서버에 연결되면 연결 메시지 출력
            System.out.println("Connected to the server at " + serverIP + ":" + serverPort);
            writer.println("START_QUIZ");  // 서버에 퀴즈 시작 메시지를 보냄

            String response;  // 서버로부터 받은 응답을 저장할 변수
            while ((response = reader.readLine()) != null) {  // 서버로부터 응답을 계속 받음
                if (response.startsWith("QUESTION")) {  // 질문 메시지를 받음
                    String question = response.split(":")[2];  // "QUESTION:{문제 번호}:{문제 내용}"에서 문제 내용을 추출
                    System.out.println(question);  // 문제 출력
                    System.out.print("Your answer: ");  // 사용자에게 답을 입력하라고 안내
                    String answer = userInput.readLine();  // 사용자가 입력한 답을 받음

                    // 답이 비어있으면 다시 입력받도록 안내
                    if (answer == null || answer.trim().isEmpty()) {
                        System.out.println("Invalid input. Please enter an answer.");
                        continue;  // 잘못된 입력이면 다시 질문을 받음
                    }

                    writer.println("ANSWER:" + answer);  // 답안을 서버에 전송
                } else if (response.startsWith("FEEDBACK")) {  // 피드백을 받음
                    System.out.println(response.split(":")[1]);  // "FEEDBACK:정답 여부"에서 피드백 출력
                } else if (response.startsWith("FINAL_SCORE")) {  // 퀴즈가 끝났을 때 점수를 받음
                    System.out.println("Final Score: " + response.split(":")[1]);  // 최종 점수 출력
                    break;  // 퀴즈 종료, 프로그램 종료
                }
            }
        } catch (UnknownHostException e) {  // 서버가 존재하지 않으면 발생
            System.err.println("Error: Unknown host " + serverIP);
        } catch (IOException e) {  // 서버와의 연결에 실패하면 발생
            System.err.println("Error: Unable to connect to the server at " + serverIP + ":" + serverPort);
            e.printStackTrace();
        }
    }

    // 서버 설정 파일에서 IP와 포트 정보를 읽어오는 함수
    private static void loadServerConfig() {
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                serverIP = reader.readLine().trim();  // 첫 번째 줄에 서버 IP 주소를 읽음
                serverPort = Integer.parseInt(reader.readLine().trim());  // 두 번째 줄에 포트 번호를 읽음
            } catch (IOException e) {
                System.out.println("Error reading config file, using default values.");
            }
        }
    }
}
