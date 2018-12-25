package experiment.diary;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiaryListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listview;
    List<Map<String, Object>> list2 = new ArrayList<>();

    ArrayList<Diary> list3 = new ArrayList<>();

    private Map<Integer, Integer> monthTranslator = new HashMap<>();
    private Map<Integer,Integer> idTranslator = new HashMap<>();
    private ArrayList<Diary> monthDiaryList= new ArrayList<>();
    private DatabaseAdapter databaseAdapter = new DatabaseAdapter(DiaryListActivity.this);
    private DrawerLayout drawer;
    Calendar c = Calendar.getInstance();
    int selectedMonth = 0;
    int currentYear = 0;
    TextView showmonth;
    MyAdapter myAdapter;
    int hourSetBefore= 0;
    int minuteSetBefore = 0;

    TimeAlertDialog timeAlertDialog =null;
    TimePicker timePicker = null;



    //ADD
    private SharedPreferences sp;
    boolean isStartService = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selectedMonth = c.get(Calendar.MONTH) + 1;
        currentYear = c.get(Calendar.YEAR);
        showmonth = (TextView) findViewById(R.id.showMonth);
        showmonth.setText(currentYear + "年" + selectedMonth + "月");


        //ADD
        sp = getSharedPreferences("UserNote", MODE_PRIVATE);
        //ADD
        listview = (ListView) findViewById(R.id.list_diary);


        monthTranslator.put(R.id.Jauary, 1);
        monthTranslator.put(R.id.February, 2);
        monthTranslator.put(R.id.March, 3);
        monthTranslator.put(R.id.April, 4);
        monthTranslator.put(R.id.May, 5);
        monthTranslator.put(R.id.June, 6);
        monthTranslator.put(R.id.July, 7);
        monthTranslator.put(R.id.August, 8);
        monthTranslator.put(R.id.September, 9);
        monthTranslator.put(R.id.October, 10);
        monthTranslator.put(R.id.November, 11);
        monthTranslator.put(R.id.December, 12);

        idTranslator.put(1,R.id.Jauary);
        idTranslator.put(2,R.id.February);
        idTranslator.put(3,R.id.March);
        idTranslator.put(4,R.id.April);
        idTranslator.put(5,R.id.May);
        idTranslator.put(6,R.id.June);
        idTranslator.put(7,R.id.July);
        idTranslator.put(8,R.id.August);
        idTranslator.put(9,R.id.September);
        idTranslator.put(10,R.id.October);
        idTranslator.put(11,R.id.November);
        idTranslator.put(12,R.id.December);

        final TextView showIsAlarmOn = (TextView) findViewById(R.id.showIsAlarmOn);

        monthDiaryList.clear();
        monthDiaryList.addAll(databaseAdapter.queryDiaryByMonth(selectedMonth));
        myAdapter = new MyAdapter(this, monthDiaryList);
        listview.setAdapter(myAdapter);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {                       //删除修改
                final TextView record_date = (TextView) view.findViewById(R.id.record_date);
                final int date = Integer.parseInt(record_date.getText().toString());
                new AlertDialog.Builder(DiaryListActivity.this)
                        .setMessage("确定删除" + selectedMonth + "月" + record_date.getText().toString() +
                                "日的日记？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseAdapter databaseAdapter = new DatabaseAdapter(DiaryListActivity.this);
                                databaseAdapter.deleteDiary(selectedMonth, date);
                                monthDiaryList.clear();
                                monthDiaryList.addAll(databaseAdapter.queryDiaryByMonth(selectedMonth));
                                myAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return false;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {              //点击查看
                final TextView record_date = (TextView) view.findViewById(R.id.record_date);
                final int date = Integer.parseInt(record_date.getText().toString());
                Intent intent = new Intent(DiaryListActivity.this, EditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("month", selectedMonth);
                bundle.putInt("day", date);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
        });
/*        final SimpleAdapter simpleAdapter = new SimpleAdapter(this,getData(),R.layout.diary_item,
                new String[]{"record_date","record_content","record_img"},
                new int[]{R.id.record_date,R.id.record_content,R.id.record_img});
        listview.setAdapter(simpleAdapter);*/

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        LayoutInflater inflater = getLayoutInflater();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final View layout = inflater.inflate(R.layout.time_dialog, drawerLayout, false);

/*        final AlertDialog.Builder memorandumBuilder = new AlertDialog.Builder(DiaryListActivity.this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factory = LayoutInflater.from(DiaryListActivity.this);
                final View view1 = factory.inflate(R.layout.layout_dialog,null);
                //对话框
                memorandumBuilder.setTitle("添加备忘录").setView(view1).setNegativeButton("取消",null).setPositiveButton("确定",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //添加备忘录
                    }
                }).show();
            }
        });*/
        isStartService = sp.getBoolean("isStartService",false);
        if (isStartService) {
            showIsAlarmOn.setText(getResources().getString(R.string.AlarmOn));
        }
        else {
            showIsAlarmOn.setText(getResources().getString(R.string.AlarmOff));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeAlertDialog == null) {
                    timeAlertDialog = new TimeAlertDialog(DiaryListActivity.this);
                    timePicker = (TimePicker) layout.findViewById(R.id.timePicker);
                }
                final Switch aswitch = (Switch) layout.findViewById(R.id.aswitch);
                timePicker = (TimePicker) layout.findViewById(R.id.timePicker);
                timeAlertDialog.setPositiveButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (aswitch.isChecked()) {
                            Toast.makeText(DiaryListActivity.this, "提醒时间设置为：" + timeAlertDialog.getHour() + " 时 " +
                                    timeAlertDialog.getMinute() + " 分 ", Toast.LENGTH_LONG).show();
                            showIsAlarmOn.setText(getResources().getString(R.string.AlarmOn));
                            //ADD
                            Intent ServiceIntent = new Intent(DiaryListActivity.this, LongRunningService.class);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt("setHour", timeAlertDialog.getHour());
                            editor.putInt("setMinute", timeAlertDialog.getMinute());
                            editor.putBoolean("isStartService", aswitch.isChecked());
                            editor.commit();
                            startService(ServiceIntent);
                            //ADD
                            //ADD else
                        } else {
                            if (sp.getBoolean("isStartService", false)) {
                                showIsAlarmOn.setText(getResources().getString(R.string.AlarmOff));
                                Intent ServiceIntent = new Intent(DiaryListActivity.this, LongRunningService.class);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean("isStartService", aswitch.isChecked());
                                editor.commit();
                                Toast.makeText(DiaryListActivity.this, "Stop Service", Toast.LENGTH_LONG).show();
                                stopService(ServiceIntent);
                            }
                        }
                        //ADD else
                        timeAlertDialog.dismiss();
                    }
                });
                timeAlertDialog.setNegativeButton(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        timeAlertDialog.dismiss();
                    }
                });
                timeAlertDialog.setSwitch(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (aswitch.isChecked()) {
                            aswitch.setChecked(false);
                            timeAlertDialog.setPickerEnabled(false);
                            timeAlertDialog.setAlpha(0.4f);
                        } else {
                            aswitch.setChecked(true);
                            timeAlertDialog.setPickerEnabled(true);
                            timeAlertDialog.setAlpha(1.0f);
                        }
                    }
                });
                timeAlertDialog.show();
                isStartService = sp.getBoolean("isStartService",false);
                if (isStartService) {
                    hourSetBefore = sp.getInt("setHour",0);
                    minuteSetBefore = sp.getInt("setMinute",0);
                    timeAlertDialog.setChecked(true);
                    aswitch.setChecked(true);
                    timeAlertDialog.setPickerEnabled(true);
                    timeAlertDialog.setAlpha(1.0f);
                    timeAlertDialog.setHour(hourSetBefore);
                    timeAlertDialog.setMinute(minuteSetBefore);
                    showIsAlarmOn.setText(getResources().getString(R.string.AlarmOn));
                }
                else {
                    timeAlertDialog.setChecked(false);
                    aswitch.setChecked(false);
                    timeAlertDialog.setPickerEnabled(false);
                    timeAlertDialog.setAlpha(0.4f);
                    showIsAlarmOn.setText(getResources().getString(R.string.AlarmOff));
                }
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();





        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(idTranslator.get(selectedMonth));
        View headerView = navigationView.getHeaderView(0);
        ImageButton foldNavBtn = (ImageButton) headerView.findViewById(R.id.foldNavBtn);
        foldNavBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent mIntent = new Intent(DiaryListActivity.this,EditActivity.class);
            startActivityForResult(mIntent,0);
            return false;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.Jauary) {
//            // Handle the camera action
//            //ADD BY JIAJUN
//            selectedMonth = 1;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.February) {
//            //ADD BY JIAJUN
//            selectedMonth = 2;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.March) {
//            //ADD BY JIAJUN
//            selectedMonth = 3;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.April) {
//            //ADD BY JIAJUN
//            selectedMonth = 4;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.May) {
//            //ADD BY JIAJUN
//            selectedMonth = 5;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.June) {
//            //ADD BY JIAJUN
//            selectedMonth = 6;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.July) {
//            //ADD BY JIAJUN
//            selectedMonth = 7;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.August) {
//            //ADD BY JIAJUN
//            selectedMonth = 8;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.September) {
//            //ADD BY JIAJUN
//            selectedMonth = 9;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.October) {
//            //ADD BY JIAJUN
//            selectedMonth = 10;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.November) {
//            //ADD BY JIAJUN
//            selectedMonth = 11;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        } else if (id == R.id.December) {
//            //ADD BY JIAJUN
//            selectedMonth = 12;
//            showmonth.setText(currentYear+"年"+selectedMonth+"月");
//            myAdapter.notifyDataSetChanged();
//            //ADD BY JIAJUN
//        }
        selectedMonth = monthTranslator.get(id);
        monthDiaryList.clear();
        monthDiaryList.addAll(databaseAdapter.queryDiaryByMonth(selectedMonth));
        Log.i("tempLog", "onNavigationItemSelected: " + monthDiaryList);
        showmonth.setText(currentYear+"年"+selectedMonth+"月");
        myAdapter.notifyDataSetChanged();


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private List<Map<String,Object>> getData() {
        Integer[] imgs = new Integer[] {R.drawable.test01,R.drawable.test02,R.drawable.test03,R.drawable.test04,R.drawable.test05,R.drawable.test06,R.drawable.test07,R.drawable.test08,R.drawable.test09};
        Map<String, Object> map = new HashMap<>();
        map.put("record_date","27");
        map.put("record_content","aa");
        map.put("record_img",imgs[0]);
        list2.add(map);

        map = new HashMap<>();
        map.put("record_date","22");
        map.put("record_content","aa");
        map.put("record_img",imgs[1]);
        list2.add(map);


        return list2;
    }

//    public ArrayList<Diary> getDataByDB() {
//        DatabaseAdapter databaseAdapter = new DatabaseAdapter(DiaryListActivity.this);
//        return databaseAdapter.queryDiaryByMonth(selectedMonth);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            monthDiaryList.clear();
            monthDiaryList.addAll(databaseAdapter.queryDiaryByMonth(selectedMonth));
            myAdapter.notifyDataSetChanged();
        }
    }

}
