package kr.co.fintalk.fintalkone.ui.calculator.saving;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.oooobang.library.OBParse;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.co.fintalk.fintalkone.R;
import kr.co.fintalk.fintalkone.common.BaseFragmentActivity;
import kr.co.fintalk.fintalkone.common.ClearEditText;
import kr.co.fintalk.fintalkone.common.DecimalDigitsInputFilter;
import kr.co.fintalk.fintalkone.common.FTConstants;

/**
 * Created by BeomyongChoi on 6/27/16
 */
public class SavingThirdActivity extends BaseFragmentActivity {
    OBParse mParse = new OBParse();

    ClearEditText mDepositAmountEditText;
    ClearEditText mDepositPeriodEditText;
    ClearEditText mInterestRateEditText;

    Button mCalculatorButton;

    public int mInterestType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saving_third);

        TextView titleTextView = (TextView) findViewById(R.id.appbarTitle);
        titleTextView.setText(R.string.saving_deposit);

        mDepositAmountEditText = (ClearEditText) findViewById(R.id.savingDepositAmountEditText);
        mDepositPeriodEditText = (ClearEditText) findViewById(R.id.savingDepositPeriodEditText);
        mInterestRateEditText = (ClearEditText) findViewById(R.id.savingInterestRateEditText);

        mDepositAmountEditText.setWon(true);
        mDepositPeriodEditText.setPeriod(true);
        mDepositAmountEditText.setSaving(true);
        mInterestRateEditText.setRate(true);

        mCalculatorButton = (Button) findViewById(R.id.calculatorButton);

        mInterestRateEditText.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(3, 3)});
        mInterestRateEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);

        mInterestRateEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mCalculatorButton.performClick();
                }
                return true;
            }
        });
    }

    public void interestTypeOnClick(View view) {
        final CharSequence[] items = new CharSequence[]{"단리", "월복리", "연복리"};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.interest_type);
        dialog.setItems(items,new DialogInterface.OnClickListener() {
            // 리스트 선택 시 이벤트
            public void onClick(DialogInterface dialog, int id) {
                TextView interestTypeTextView = (TextView) findViewById(R.id.savingInterestTypeTextView);
                interestTypeTextView.setText(items[id]);
                mInterestType = id;
            }
        });
        dialog.show();
    }

    public void calculateSavingOnClick(View view) {
        String depositAmountString = mDepositAmountEditText.getText().toString();
        String depositPeriodString = mDepositPeriodEditText.getText().toString();
        String interestRateString = mInterestRateEditText.getText().toString();

        double depositAmount = mParse.toDouble(depositAmountString.replace("원","").replace(",",""));
        double depositPeriod = mParse.toDouble(depositPeriodString.replace("개월",""));
        double interestRate = mParse.toDouble(interestRateString.replace("%",""));

        double resultInterest;


        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);


        if(depositAmountString.length() != 0
                && depositPeriodString.length() != 0
                && interestRateString.length() != 0) {
            inputMethodManager.hideSoftInputFromWindow(mInterestRateEditText.getWindowToken(), 0);
            mDepositAmountEditText.clearFocus();
            mDepositPeriodEditText.clearFocus();
            mInterestRateEditText.clearFocus();
            resultInterest = calculatorInterest(depositAmount, depositPeriod, interestRate);
            setResultHeader(mParse.addComma(depositAmount) + "원");
            setListView(depositAmount, resultInterest);
        }
        else {
            showToast(R.string.toast_text, 2);
        }
    }

    public Map<String,?> createItem(String title, String contents) {
        Map<String,String> item = new HashMap<>();
        item.put(FTConstants.ITEM_TITLE, title);
        item.put(FTConstants.ITEM_CONTENTS, contents);
        return item;
    }

    public void setResultHeader(String principal) {
        TextView titleTextView = (TextView) findViewById(R.id.resultTitleTextView);
        TextView captionTextView = (TextView) findViewById(R.id.resultCaptionTextView);
        TextView amountTextView = (TextView) findViewById(R.id.resultAmountTextView);

        titleTextView.setText(R.string.result_calculate);
        captionTextView.setText(R.string.principal);
        amountTextView.setText(principal);
    }

    public void setListView(double principal, double taxFreeInterest) {
        double taxGeneralInterest, taxBreaksInterest;
        double taxGeneral, taxBreaks;

        taxGeneralInterest = taxFreeInterest*.846;
        taxBreaksInterest = taxFreeInterest*.905;

        taxGeneral = taxFreeInterest*.154;
        taxBreaks = taxFreeInterest*.095;

        //일반과세
        String taxGeneralAmountString = mParse.addComma(principal + taxGeneralInterest) + "원";
        String taxGeneralInterestString = mParse.addComma(taxGeneralInterest) + "원";
        String taxGeneralString = mParse.addComma(taxGeneral) + "원";
        //세금우대
        String taxBreaksAmountString = mParse.addComma(principal + taxBreaksInterest) + "원";
        String taxBreaksInterestString = mParse.addComma(taxBreaksInterest) + "원";
        String taxBreaksString = mParse.addComma(taxBreaks) + "원";
        //비과세
        String taxFreeAmountString = mParse.addComma(principal + taxFreeInterest) + "원";
        String taxFreeInterestString = mParse.addComma(taxFreeInterest) + "원";

        List<Map<String,?>> taxGeneralList = new LinkedList<>();
        taxGeneralList.add(createItem("만기지급액", taxGeneralAmountString));
        taxGeneralList.add(createItem("세후이자", taxGeneralInterestString));
        taxGeneralList.add(createItem("세금", taxGeneralString));

        List<Map<String,?>> taxBreaksList = new LinkedList<>();
        taxBreaksList.add(createItem("만기지급액", taxBreaksAmountString));
        taxBreaksList.add(createItem("세후이자", taxBreaksInterestString));
        taxBreaksList.add(createItem("세금", taxBreaksString));

        List<Map<String,?>> taxFreeList = new LinkedList<>();
        taxFreeList.add(createItem("만기지급액", taxFreeAmountString));
        taxFreeList.add(createItem("세후이자", taxFreeInterestString));
        taxFreeList.add(createItem("세금", "0원"));

        // create our list and custom adapter
        SavingListViewAdapter adapter = new SavingListViewAdapter(this);

        String[] from = { FTConstants.ITEM_TITLE, FTConstants.ITEM_CONTENTS };
        int[] to = new int[] {R.id.savingResultTitle, R.id.savingResultContents};

        adapter.addSection("일반과세", new SimpleAdapter(this, taxGeneralList,
                R.layout.listview_saving_row, from, to));
        adapter.addSection("세금우대", new SimpleAdapter(this, taxBreaksList,
                R.layout.listview_saving_row, from, to));
        adapter.addSection("비과세", new SimpleAdapter(this, taxFreeList,
                R.layout.listview_saving_row, from, to));

        ListView list = (ListView) findViewById(R.id.savingResultListView);
        list.setAdapter(adapter);
    }

    public double calculatorInterest(double payment, double period, double yearlyRate) {
        yearlyRate /= 100;
        double monthlyRate = yearlyRate / 12;
        double yearlyPeriod = (period + 1) / 12;
        double paymentCountMonthly = period * (period + 1) / 2;

        double monthlyRoot = 1 + monthlyRate;
        double yearlyRoot = 1 + yearlyRate;
        double principal = payment * period;

        double yearlyConstant = Math.pow(yearlyRoot, 1.0 / 12);

        double simpleMultiplier = paymentCountMonthly * monthlyRate;
        double monthlyMultiplier = Math.pow(monthlyRoot, period) - 1;
        double yearlyMultiplier = Math.pow(yearlyRoot, yearlyPeriod) - yearlyConstant;


        switch (mInterestType) {
            case 0: // 단리
                return payment * simpleMultiplier;
            case 1: // 월복리
                return payment * monthlyRoot * monthlyMultiplier / monthlyRate - principal;
            case 2: // 연복리
                return payment * yearlyMultiplier / (yearlyConstant - 1) - principal;
            default:// 오리
                return 0;
        }
    }
}