package com.bignerdranch.android.databasetest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Base1Activity extends Activity {
    private MySQLiteOpenHelper dbHelper = null;
    private TextView emptyText;
    private ListView lv_main;
    private SimpleAdapter adapter = null;
    private List<Map<String, Object>> totalList = new ArrayList<Map<String, Object>>();
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //调用父类的OnCreate()方法，保存失去焦点时的状态
        setContentView(R.layout.activity_base1);           //传入activity_base1布局
        dbHelper = new MySQLiteOpenHelper(this);
        lv_main = (ListView) findViewById(R.id.listView_main);       //遍历
        emptyText = (TextView) findViewById(R.id.textView_empty);
        totalList = getcontent();  //检索所有条目，准备数据源

        adapter = new SimpleAdapter(this, totalList, R.layout.item_listview_main,
                new String[] { "phonenumber", "username" },
                new int[] { R.id.textView_item_phonenumber, R.id.textView_item_username });
//        第一个参数：上下文对象
//        第二个参数：数据源是含有Map的一个集合
//        第三个参数：每一个item的布局文件
//        第四个参数：new String[]{}数组，数组的里面的每一项要与第二个参数中的存入map集合的的key值一样，一一对应
//        第五个参数：new int[]{}数组，数组里面的第三个参数中的item里面的控件id

        lv_main.setAdapter(adapter);    //给listview设置adapter数据适配器
        lv_main.setEmptyView(emptyText);   //提示视图
        registerForContextMenu(lv_main);   //注册上下文菜单
//在6.0版本之后即使加入权限也会崩溃 存在动态权限获取问题
        lv_main.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {      //listview监听事件   参数arg2指在listview中的位置
                final String number = totalList.get(arg2).get("phonenumber").toString();
                Builder builder = createAlertDialog(android.R.drawable.stat_sys_phone_call, "确定要拨打: " + number + " ？");
                if (ContextCompat.checkSelfPermission(Base1Activity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // 没有获得授权，申请授权
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Base1Activity.this,
                            Manifest.permission.CALL_PHONE)) {
                        // 返回值：
//                          如果app之前请求过该权限,被用户拒绝, 这个方法就会返回true.
//                          如果用户之前拒绝权限的时候勾选了对话框中”Don’t ask again”的选项,那么这个方法会返回false.
//                          如果设备策略禁止应用拥有这条权限, 这个方法也返回false.
                        // 弹窗需要解释为何需要该权限，再次请求授权
                        Toast.makeText(Base1Activity.this, "请授权！", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        // 帮跳转到该应用的设置界面，让用户手动授权
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else {
                        // 不需要解释为何需要该权限，直接请求授权
                        ActivityCompat.requestPermissions(Base1Activity.this,
                                new String[]{Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);
                    }
                }else    //已经获得授权
                    {
                        builder.setPositiveButton("拨打", new OnClickListener() {   //确认拨打的监听事件
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setAction(Intent.ACTION_CALL);        // 直接拨打电话uri
                                intent.setData(Uri.parse("tel:" + number));
                                startActivity(intent);
                            }
                        });
                    }
                    builder.show();
            }
        });
    }

    private List<Map<String, Object>> getcontent() {
        return dbHelper.selectList("select * from tb_mycontacts", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);            //得到MenuInflater对象，再调用它的inflate()方法就可以给当前的活动创建菜单
        //（指定通过哪一个资源文件来创建菜单，指定菜单项将添加到哪一个Menu对象当中）
        return true;        //返回true，表示允许创建的菜单显示出来
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {    //主界面标题菜单栏监听
        switch (item.getItemId()) {             //item.getItemId()判断点击的是哪一个菜单项
            case R.id.action_insert:
                Builder builder_insert = createAlertDialog(android.R.drawable.ic_dialog_alert, "添加联系人信息");
                View view = getLayoutInflater().inflate(R.layout.dialog_insert, null);
                final EditText et_name = (EditText) view.findViewById(R.id.editText_dialog_name);
                final EditText et_number = (EditText) view.findViewById(R.id.editText_dialog_number);

                builder_insert.setView(view);
                builder_insert.setPositiveButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = et_name.getText() + "";
                        String number = et_number.getText() + "";
                        if (name.equals("") || number.equals("")) {
                            toast("输入信息不能为空！");
                        } else {
                            String sql = "insert into tb_mycontacts(username, phonenumber)values(?,?)";
                            boolean flag = dbHelper.execData(sql, new Object[] { name, number });  //更新执行
                            if (flag) {
                                toast("插入成功！");
                                reloadView();
                            } else {
                                toast("插入失败！");
                            }
                        }
                    }
                });
                builder_insert.show();
                break;
            case R.id.action_deleteAll:
                Builder builder_delete = createAlertDialog(android.R.drawable.ic_menu_delete, "确定要删除所有数据？");
                builder_delete.setPositiveButton("删除", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String sql = "delete from tb_mycontacts";
                        boolean flag = dbHelper.deleteData(sql);
                        if (flag) {
                            toast("删除所有数据成功！");
                            reloadView();
                        } else {
                            toast("删除所有数据失败！");
                        }
                    }
                });
                builder_delete.show();
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  //每个listview的上下文菜单
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        menu.setHeaderIcon(android.R.drawable.btn_dialog);
        String name = totalList.get(info.position).get("username").toString();  //获取点击listview对应的信息
        String number = totalList.get(info.position).get("phonenumber").toString();
        menu.setHeaderTitle(name + "|" + number);
        getMenuInflater().inflate(R.menu.contextmenu_listview_main, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) { //上下文菜单实现监听
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();   //应用程序需要知道View条目的中的信息
        String name = totalList.get(info.position).get("username").toString();
        String number = totalList.get(info.position).get("phonenumber").toString();
        final String _id = totalList.get(info.position).get("_id").toString();  //获取要操作的id
        switch (item.getItemId()) {
            case R.id.action_delete:
                Builder builder_dele = createAlertDialog(android.R.drawable.ic_delete, "确定要删除？");
                builder_dele.setPositiveButton("删除", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {  //删除对应条目
                        String sql = "delete from tb_mycontacts where _id=?";
                        boolean flag = dbHelper.execData(sql, new Object[] { _id });
                        if (flag) {
                            toast("删除数据成功！");
                            reloadView();
                        } else {
                            toast("删除数据失败！");
                        }
                    }
                });
                builder_dele.show();
            break;

            case R.id.action_update:
                Builder builder_update = createAlertDialog(android.R.drawable.ic_dialog_alert, "修改联系人信息");
                View view = getLayoutInflater().inflate(R.layout.dialog_update, null);
                final EditText editText_name = (EditText) view.findViewById(R.id.editText_dialog_name);
                final EditText editText_number = (EditText) view.findViewById(R.id.editText_dialog_number);

                // 因为是更新，所以两个控件里应该有初始值
                editText_name.setText(name);
                editText_number.setText(number);

                builder_update.setView(view);

                builder_update.setPositiveButton("确认", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name_sure = editText_name.getText() + "";
                        String number_sure = editText_number.getText() + "";
                        String sql = "update tb_mycontacts set username=? , phonenumber=? where _id=?";
                        boolean flag = dbHelper.execData(sql, new Object[] { name_sure, number_sure, _id });
                        if (flag) {
                            toast("更新数据成功！");
                            reloadView();
                        } else {
                            toast("更新数据失败！");
                        }
                    }
                });
                builder_update.show();
                break;

            case R.id.action_sendsms:   //发送短信
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + number));
                intent.putExtra("sms_body", "hello!");  //添加默认信息
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    protected void reloadView() {  //刷新listView
        totalList.clear();
        totalList.addAll(getcontent());
        adapter.notifyDataSetChanged();
    }

    public void clickButton(View view) {
        lv_main.setSelection(0);
    }    //到达顶部

    protected void toast(String string) {
        Toast.makeText(Base1Activity.this, string, Toast.LENGTH_LONG).show();
    }

    private Builder createAlertDialog(int icDialogAlert, String string) {  //警告对话框
        Builder builder = new Builder(this);
        builder.setIcon(icDialogAlert);
        builder.setTitle(string);
        builder.setNegativeButton("取消", null);
        return builder;
    }
}