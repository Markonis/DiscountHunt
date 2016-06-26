package rs.elfak.mosis.marko.discounthunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {


    private Button mShowMapButton;
    private Button mSigninButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShowMapButton = (Button) findViewById(R.id.show_map);
        mShowMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMapActivity();
            }
        });

        mSigninButton = (Button) findViewById(R.id.signin);
        mSigninButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSigninActivity();
            }
        });

        if(DiscountHunt.currentUser == null) {
            mShowMapButton.setVisibility(View.GONE);
            mSigninButton.setVisibility(View.VISIBLE);
        }else{
            mShowMapButton.setVisibility(View.VISIBLE);
            mSigninButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    private void startMapActivity() {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void startSigninActivity() {
        Intent intent = new Intent(this, SigninActivity.class);
        startActivity(intent);
    }
}
