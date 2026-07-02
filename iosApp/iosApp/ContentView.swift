import ComposeApp
import KakaoSDKUser
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController { completion in
            KakaoLoginBridge.login { accessToken, errorMessage in
                _ = completion(accessToken, errorMessage)
            }
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

private enum KakaoLoginBridge {
    static func login(completion: @escaping (String?, String?) -> Void) {
        guard let appKey = Bundle.main.object(forInfoDictionaryKey: "KAKAO_NATIVE_APP_KEY") as? String,
              !appKey.isEmpty else {
            completion(nil, "Kakao Native App Key가 설정되지 않았습니다.")
            return
        }

        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk { token, error in
                if let accessToken = token?.accessToken, !accessToken.isEmpty {
                    finish(accessToken: accessToken, errorMessage: nil, completion: completion)
                } else if error != nil {
                    loginWithKakaoAccount(completion: completion)
                } else {
                    finish(accessToken: nil, errorMessage: "카카오 로그인에 실패했습니다.", completion: completion)
                }
            }
        } else {
            loginWithKakaoAccount(completion: completion)
        }
    }

    private static func loginWithKakaoAccount(completion: @escaping (String?, String?) -> Void) {
        UserApi.shared.loginWithKakaoAccount { token, error in
            finish(
                accessToken: token?.accessToken,
                errorMessage: error?.localizedDescription,
                completion: completion
            )
        }
    }

    private static func finish(
        accessToken: String?,
        errorMessage: String?,
        completion: @escaping (String?, String?) -> Void
    ) {
        DispatchQueue.main.async {
            if let accessToken, !accessToken.isEmpty {
                completion(accessToken, nil)
            } else {
                completion(nil, errorMessage ?? "카카오 로그인에 실패했습니다.")
            }
        }
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.all)
    }
}
