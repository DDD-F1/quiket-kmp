import ComposeApp
import AuthenticationServices
import KakaoSDKUser
import SwiftUI
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            kakaoLoginLauncher: { completion in
                KakaoLoginBridge.login { accessToken, errorMessage in
                    _ = completion(accessToken, errorMessage)
                }
            },
            appleLoginLauncher: { completion in
                AppleLoginBridge.login { identityToken, authorizationCode, fullName, errorMessage in
                    _ = completion(identityToken, authorizationCode, fullName, errorMessage)
                }
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

private enum AppleLoginBridge {
    private static var activeRequest: AppleLoginRequest?

    static func login(
        completion: @escaping (String?, String?, String?, String?) -> Void
    ) {
        guard activeRequest == nil else {
            completion(nil, nil, nil, "Apple 로그인이 이미 진행 중입니다.")
            return
        }

        let request = AppleLoginRequest(completion: completion)
        activeRequest = request
        request.perform()
    }

    static func finish(_ request: AppleLoginRequest) {
        if activeRequest === request {
            activeRequest = nil
        }
    }
}

private final class AppleLoginRequest: NSObject,
    ASAuthorizationControllerDelegate,
    ASAuthorizationControllerPresentationContextProviding {
    private let completion: (String?, String?, String?, String?) -> Void
    private var controller: ASAuthorizationController?

    init(completion: @escaping (String?, String?, String?, String?) -> Void) {
        self.completion = completion
    }

    func perform() {
        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        self.controller = controller
        controller.performRequests()
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization authorization: ASAuthorization
    ) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let identityToken = credential.identityToken,
              let identityTokenValue = String(data: identityToken, encoding: .utf8),
              !identityTokenValue.isEmpty else {
            complete(errorMessage: "Apple 인증 정보를 읽지 못했습니다.")
            return
        }

        let authorizationCode = credential.authorizationCode
            .flatMap { String(data: $0, encoding: .utf8) }
            .flatMap { $0.isEmpty ? nil : $0 }
        let fullName = credential.fullName
            .map { PersonNameComponentsFormatter().string(from: $0) }
            .flatMap { $0.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? nil : $0 }

        complete(
            identityToken: identityTokenValue,
            authorizationCode: authorizationCode,
            fullName: fullName,
            errorMessage: nil
        )
    }

    func authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithError error: Error
    ) {
        let nsError = error as NSError
        let message: String
        if nsError.domain == ASAuthorizationError.errorDomain,
           nsError.code == ASAuthorizationError.canceled.rawValue {
            message = "Apple 로그인이 취소되었습니다."
        } else {
            message = "Apple 로그인을 완료하지 못했습니다. 다시 시도해 주세요."
        }
        complete(errorMessage: message)
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let activeWindowScene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        return activeWindowScene?.windows.first { $0.isKeyWindow } ?? UIWindow()
    }

    private func complete(
        identityToken: String? = nil,
        authorizationCode: String? = nil,
        fullName: String? = nil,
        errorMessage: String? = nil
    ) {
        AppleLoginBridge.finish(self)
        DispatchQueue.main.async {
            self.completion(identityToken, authorizationCode, fullName, errorMessage)
        }
    }
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
