package com.example.ex1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainAcitivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // xml 설정
        setContentView(R.layout.activity_main);
        findViewById(R.id.logoutButton).setOnClickListener(onClickListener);


        // 현재 사용자 누군지 확인
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // 현재 로그인 여부를 확인해서 로그인 안되어있으면 로그인 화면으로 가는 코드
        if(user == null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }else{
            // 데이터베이스 초기화 <- Firebase Firestore db에서 회원 정보를 가져올 예정
            FirebaseFirestore db = FirebaseFirestore.getInstance();


            // 데이터베이스 users 콜렉션 안에서 사용자 회원별 uid document로 접속
            DocumentReference docRef = db.collection("users").document(user.getUid());

            // document에서 불러오는 위 쿠드가 수행 완료시 동작
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                // DocumentSnapshot 자료형인 task에 결과를 저장
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        // 정상적으로 동작 했을 경우 document로 복사
                        DocumentSnapshot document = task.getResult();

                        if(document != null) {
                            // document가 비어있을경우 확인
                            if (document.exists()) {
                                // 데이터베이스에서 회원정보 가져오기 document에 저장됨
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                // 데이터베이스에 회원정보 없으면 회원정보 입력 화면 전환
                                Log.d(TAG, "No such document");
                                startActivity(new Intent(MainActivity.this, UserInfoActivity.class));
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    // 버튼 클릭시 뭐를 동작 시킬지 구분해주는 부분
    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case R.id.logoutButton:
                    // 로그아웃 버튼 클릭시 로그아웃 후 로그인 엑티비티로 이동
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    break;
            }
        }
    };

    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간
    @Override
    public void onBackPressed() {


        //마지막으로 누른 '뒤로가기' 버튼 클릭 시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 크면
        if(System.currentTimeMillis() > pressedTime + 2000){
            //현재 시간을 pressedTime 에 저장
            pressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"한번 더 누르면 종료", Toast.LENGTH_SHORT).show();
        }

        //마지막 '뒤로가기' 버튼 클릭시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 작으면
        else{
            Toast.makeText(getApplicationContext(),"종료 완료", Toast.LENGTH_SHORT).show();
            // 앱 종료
            moveTaskToBack(true); // 태스크를 백그라운드로 이동
            finishAndRemoveTask(); // 액티비티 종료 + 태스크 리스트에서 지우기

            System.exit(0);
        }
    }

    // 토스트 창을 띄우기 위한 함수
    private void startToast( String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}