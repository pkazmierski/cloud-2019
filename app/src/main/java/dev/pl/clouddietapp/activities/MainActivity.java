package dev.pl.clouddietapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.amplify.generated.graphql.ListUserDatasQuery;

import java.util.ArrayList;
import java.util.List;

import dev.pl.clouddietapp.R;
import dev.pl.clouddietapp.logic.Logic;

public class MainActivity extends AppCompatActivity {
    private List<ListUserDatasQuery.Item> userDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Logic.initAppSync(this);
    }
}
