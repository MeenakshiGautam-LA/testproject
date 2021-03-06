package com.sisindia.mysis.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sisindia.mysis.Model.MapTableRosterAndShift;
import com.sisindia.mysis.Model.Other_Duty_Mark_Model;
import com.sisindia.mysis.PostWorkerManager.PostAttendanceWork;
import com.sisindia.mysis.R;
import com.sisindia.mysis.Services.BackgroundApiService;
import com.sisindia.mysis.activity.base.BaseActivity;
import com.sisindia.mysis.application.CSApplicationHelper;
import com.sisindia.mysis.entity.DailyAttendanceDetail;
import com.sisindia.mysis.entity.UserDetailModel;
import com.sisindia.mysis.utils.CFUtil;
import com.sisindia.mysis.utils.CSAppUtil;
import com.sisindia.mysis.utils.CSShearedPrefence;
import com.sisindia.mysis.utils.CSStringUtil;
import com.sisindia.mysis.utils.DateUtils;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MarkDuty_Confirmation_ViewScreen extends BaseActivity {
    @BindView(R.id.markAnotherDutyTV)
    TextView markAnotherDutyTV;
    @BindView(R.id.cancelBtn)
    AppCompatImageView cancelBtn;
    @BindView(R.id.userNameTV)
    TextView userNameTV;
    @BindView(R.id.postRankNameTV)
    TextView dutyRankNameTV;
    @BindView(R.id.attendanceMarkedDateTV)
    TextView attendanceMarkedDateTV;
    @BindView(R.id.lat_nd_longTV)
    TextView lat_nd_longTV;
    @BindView(R.id.shiftNameTV)
    TextView shiftNameTV;
    @BindView(R.id.unitNameTV)
    TextView unitNameTV;
    @BindView(R.id.postNameTV)
    TextView postNameTV;
    @BindView(R.id.dutyStatusTV)
    TextView dutyStatusTV;
    @BindView(R.id.dutyInTimeTV)
    TextView dutyInTimeTV;
    @BindView(R.id.descriptionTextTV)
    TextView descriptionTextTV;
    @BindView(R.id.userImageCV)
    CircleImageView userImageCV;
    @BindView(R.id.dayDescriptionTV)
    TextView dayDescriptionTV;
    @BindView(R.id.regNoTV)
    TextView regNoTV;
    private DailyAttendanceDetail dailyAttendanceDetail;
    //    private OtherUnitEmployeeDetailModel otherUnitEmployeeDetailModel;
    private Other_Duty_Mark_Model other_duty_mark_model;
    //    private UserDetailModel userDetailModelForOther;
    private Bundle bundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CFUtil.screenShotDisableInApp(this);
        CSApplicationHelper.application().setActivity(this);
        setContentView(R.layout.activity_mark_attendance_duty_in_success);
        ButterKnife.bind(this);
        cancelBtn = findViewById(R.id.cancelBtn);
        bundle = getIntent().getExtras();
        CFUtil.checkAttendanceToCallService_When_Attendance_FromOther_Duty(this);

        if (bundle != null) {
            //************************* If Condition use for mark Attendance which from Other Unit Duty Employee Punch*********************************************************
            if (bundle.getString("SCREEN_TAG").equalsIgnoreCase("OTHER_DUTY_PUNCH_SCREEN")) {
                String dutyStatus = bundle.getString("DUTY_STATUS");
                other_duty_mark_model = (Other_Duty_Mark_Model) bundle.getSerializable("OTHER_UNIT_DUTY_EMPLOYEE");
                dailyAttendanceDetail = other_duty_mark_model.getDailyAttendanceDetail();
                setOtherUnitDutyEmployeeDetail(other_duty_mark_model);
            } else {
                //*********************Use For Mark Attendance For Self Employee LOGIN**********************************************************
                String regNo = getIntent().getExtras().getString("REGNO");
                String dutyStatus = getIntent().getExtras().getString("DUTY_STATUS");
                dailyAttendanceDetail = CSApplicationHelper.application().getDatabaseInstance().markAttendanceDao().
                        getAttendanceDetail(regNo, dutyStatus, DateUtils.getCurrentDate_TIME_format());
                if (dailyAttendanceDetail != null) {
                    setUserDetail();
                    if (dailyAttendanceDetail.getDuty_status().equalsIgnoreCase("DUTY_OUT")) {
                        descriptionTextTV.setText(CSStringUtil.getString(R.string.duty_out_successfully));
                        dayDescriptionTV.setText(CSStringUtil.getString(R.string.good_bye));
                    } else {
                        descriptionTextTV.setText(CSStringUtil.getString(R.string.duty_In_successfully));
                        dayDescriptionTV.setText(CSStringUtil.getString(R.string.good_day));
                    }
                } else {
                    Toast.makeText(this, "RECORD NOT FOUND", Toast.LENGTH_SHORT).show();
                }
            }
            lat_nd_longTV.setText(CSShearedPrefence.getLatAndLongitude());
            Intent intent = new Intent(this, BackgroundApiService.class);
            intent.putExtra("message", "POST_ATTENDANCE_WHEN_MARK_ATTENDANCE");
            startService(intent);
        }
        Drawable mIcon = ContextCompat.getDrawable(this, R.drawable.icon_cross);

        if (CSShearedPrefence.isNightModeEnabled()) {
            cancelBtn.setImageDrawable(mIcon);

        } else {
            cancelBtn.setColorFilter(ContextCompat.getColor(this, R.color.black), PorterDuff.Mode.MULTIPLY);
            cancelBtn.setImageDrawable(mIcon);

        }
    }


    @OnClick({R.id.cancelBtn, R.id.markAnotherDutyTV})
    public void onClickMethod(View v) {

        switch (v.getId()) {
            case R.id.cancelBtn:
                onBackPressed();
                break;
            case R.id.markAnotherDutyTV:
                if (bundle.getString("SCREEN_TAG").equalsIgnoreCase("OTHER_DUTY_PUNCH_SCREEN")) {
                    onBackPressed();
                } else {
                    CSShearedPrefence.setJugaaar("DONE");
                    onBackPressed();
                }
                break;
        }
    }

    @Override
    public int getScreenName() {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    private void setUserDetail() {
        UserDetailModel userDetailModel = CSApplicationHelper.application().getDatabaseInstance().user_detail_dao().
                getUserDetails(dailyAttendanceDetail.getREGNO());
        if (userDetailModel != null) {
            userNameTV.setText(userDetailModel.getName());
            regNoTV.setText(CSStringUtil.getString(R.string.reg_number) + " " + userDetailModel.getRegNo());
            if (CSApplicationHelper.application().getDatabaseInstance().serviceDAO().
                    gertServiceDetail(dailyAttendanceDetail.getDUTYRANK()) != null) {
                dutyRankNameTV.setText(CFUtil.convertString(CSApplicationHelper.application().getDatabaseInstance().serviceDAO().
                        gertServiceDetail(dailyAttendanceDetail.getDUTYRANK()).getSERVICENAME()));
            }
            postNameTV.setText(CSApplicationHelper.application().getDatabaseInstance().duty_post_detail_dao().
                    postName(dailyAttendanceDetail.getDUTYPOSTID()));
            String shiftName = CSApplicationHelper.application().getDatabaseInstance().shifMaster_dao().
                    getShiftnameBy_ID(dailyAttendanceDetail.getSHIFTID());
            shiftNameTV.setText(shiftName);
            SimpleDateFormat sdf1 = DateUtils.getInstance().getDate_And_MonthWordFormat();
            String shownDay = sdf1.format(DateUtils.getInstance().dateFromShiftDate(DateUtils.getCurrentDate_format()));
            String dayOfWeek = DateUtils.getInstance().getWeek_WordFormat().format(DateUtils.getInstance().dateFromShiftDate(DateUtils.getCurrentDate_format()));
            attendanceMarkedDateTV.setText(dayOfWeek + " " + shownDay);
            dutyStatusTV.setText(dailyAttendanceDetail.getDuty_status().replaceAll("_", " ").toUpperCase());
            dutyInTimeTV.setText(DateUtils.getCurrentDate_TIME_format().split(" ")[1].split(":")[0] + ":"
                    + DateUtils.getCurrentDate_TIME_format().split(" ")[1].split(":")[1]);
            if (CSApplicationHelper.application().getDatabaseInstance().attendancePicDao().
                    getAttendanceGuardPicModel(dailyAttendanceDetail.getID(), dailyAttendanceDetail.getREGNO()) != null) {
                Glide.with(this).load(CSApplicationHelper.application().getDatabaseInstance().attendancePicDao().
                        getAttendanceGuardPicModel(dailyAttendanceDetail.getID(), dailyAttendanceDetail.getREGNO()).
                        getPicture())
                        .dontAnimate()
                        .thumbnail(0.5f).
                        diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .into(userImageCV);
            } else {
                Glide.with(this).load(R.drawable.no_image_found).
                        diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                        .dontAnimate()
                        .thumbnail(0.5f).
                        into(userImageCV);
            }
            // It use for get UNIT Name  from roster table
            MapTableRosterAndShift mapTableRosterAndShift = CSApplicationHelper.application().getDatabaseInstance().rosterDetailDao().
                    getShiftDetailOnRegNoBase(dailyAttendanceDetail.getREGNO());
            unitNameTV.setText(mapTableRosterAndShift.getUNIT_NAME() + " (" + mapTableRosterAndShift.getUNIT_CODE() + ")");
        }
    }

    @SuppressLint("SetTextI18n")
    private void setOtherUnitDutyEmployeeDetail(Other_Duty_Mark_Model other_duty_mark_model) {
        if (bundle.getString("DUTY_STATUS").equalsIgnoreCase("DUTY_OUT")) {
            descriptionTextTV.setText(CSStringUtil.getString(R.string.duty_out_successfully));
            dayDescriptionTV.setText(CSStringUtil.getString(R.string.good_bye));
            dutyStatusTV.setText(CSStringUtil.getString(R.string.duty_out).toUpperCase());
        } else {
            descriptionTextTV.setText(CSStringUtil.getString(R.string.duty_In_successfully));
            dayDescriptionTV.setText(CSStringUtil.getString(R.string.good_day));
            dutyStatusTV.setText(CSStringUtil.getString(R.string.duty_in).toUpperCase());
        }
        userNameTV.setText(other_duty_mark_model.getEmployeDetailsModel().getEMPNAME());
        if (other_duty_mark_model.getRosterDetail() != null) {
            if (!CSStringUtil.isEmptyStr(other_duty_mark_model.getRosterDetail().getDUTYSYMBOL())) {
                dutyRankNameTV.setText(CFUtil.convertString(other_duty_mark_model.getRosterDetail().getDUTYSYMBOL()));
            }

            postNameTV.setText(other_duty_mark_model.getRosterDetail().getDUTYPOSTNAME());
            shiftNameTV.setText(other_duty_mark_model.getRosterDetail().getSHIFTNAME());
            unitNameTV.setText(other_duty_mark_model.getRosterDetail().getUNITNAME() + " (" + other_duty_mark_model.getRosterDetail().getUNITCODE() + ")");

        } else {
            if (other_duty_mark_model.getDailyAttendanceDetail() != null) {
                if (CSApplicationHelper.application().getDatabaseInstance().serviceDAO()
                        .getSymbol(other_duty_mark_model.getDailyAttendanceDetail().getDUTYRANK()) != null) {
                    dutyRankNameTV.setText(CFUtil.convertString(CSApplicationHelper.application().getDatabaseInstance().serviceDAO()
                            .getSymbol(other_duty_mark_model.getDailyAttendanceDetail().getDUTYRANK())));
                }

                postNameTV.setText(other_duty_mark_model.getDailyAttendanceDetail().getDUTY_POST_NAME());
                shiftNameTV.setText(other_duty_mark_model.getDailyAttendanceDetail().getOTHER_DUTY_SHIFT_NAME());
                unitNameTV.setText(other_duty_mark_model.getDailyAttendanceDetail().getOTHER_DUTY_UNIT_NAME() + " (" + other_duty_mark_model.
                        getDailyAttendanceDetail()
                        .getUNITCODE() + ")");
            }
        }
        SimpleDateFormat sdf1 = DateUtils.getInstance().getDate_And_MonthWordFormat();
        String shownDay = sdf1.format(DateUtils.getInstance().dateFromShiftDate(DateUtils.getCurrentDate_format()));
        String dayOfWeek = DateUtils.getInstance().getWeek_WordFormat().format(DateUtils.getInstance().
                dateFromShiftDate(DateUtils.getCurrentDate_format()));
        regNoTV.setText(dailyAttendanceDetail.getREGNO());
        attendanceMarkedDateTV.setText(dayOfWeek + " " + shownDay);

        dutyInTimeTV.setText(DateUtils.getCurrentDate_TIME_format().split(" ")[1].split(":")[0] + ":"
                + DateUtils.getCurrentDate_TIME_format().split(" ")[1].split(":")[1]);

        if (CSApplicationHelper.application().getDatabaseInstance().attendancePicDao().
                getAttendanceGuardPicModel(dailyAttendanceDetail.getID(), dailyAttendanceDetail.getREGNO()) != null) {
            Glide.with(this).load(CSApplicationHelper.application().getDatabaseInstance().attendancePicDao().
                    getAttendanceGuardPicModel(dailyAttendanceDetail.getID(), dailyAttendanceDetail.getREGNO()).getPicture())
                    .error(R.drawable.no_image_found).
                    diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontAnimate()
                    .thumbnail(0.5f)
                    .into(userImageCV);
        } else {
            Glide.with(this).load(R.drawable.no_image_found).
                    diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true)
                    .dontAnimate()
                    .thumbnail(0.5f).
                    into(userImageCV);
        }
    }



}

