<?xml version="1.0" encoding="utf-8"?>
<!--
     Copyright (C) 2019 The Android Open Source Project

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
-->
<merge xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:app="http://schemas.android.com/apk/res-auto"
  xmlns:tools="http://schemas.android.com/tools"
  android:layout_width="match_parent"
  android:layout_height="@dimen/mtrl_calendar_header_height_fullscreen">

  <LinearLayout
    style="?attr/materialCalendarHeaderLayout"
    android:layout_width="match_parent"
    android:layout_height="@dimen/mtrl_calendar_header_height_fullscreen"
    android:layout_gravity="top|center_horizontal"
    android:orientation="horizontal"
    android:paddingStart="@dimen/mtrl_calendar_header_content_padding_fullscreen"
    android:paddingLeft="@dimen/mtrl_calendar_header_content_padding_fullscreen"
    android:paddingTop="@dimen/mtrl_calendar_header_content_padding_fullscreen"
    android:paddingEnd="@dimen/mtrl_calendar_header_content_padding_fullscreen"
    android:paddingRight="@dimen/mtrl_calendar_header_content_padding_fullscreen"
    tools:ignore="Overdraw">

    <com.google.android.material.button.MaterialButton
      android:id="@+id/cancel_button"
      style="?attr/materialCalendarHeaderCancelButton"
      android:layout_width="@dimen/mtrl_min_touch_target_size"
      android:layout_height="@dimen/mtrl_min_touch_target_size"
      android:layout_gravity="top"
      android:contentDescription="@string/mtrl_picker_cancel"
      android:gravity="center"
      android:insetTop="0dp"
      android:insetBottom="0dp"
      android:padding="12dp"
      app:icon="@drawable/material_ic_clear_black_24dp"
      app:rippleColor="@color/mtrl_btn_ripple_color"/>

    <FrameLayout
      android:id="@+id/mtrl_picker_header_title_and_selection"
      android:layout_width="0dp"
      android:layout_height="match_parent"
      android:layout_weight="1"
      android:paddingStart="@dimen/mtrl_calendar_header_text_padding"
      android:paddingLeft="@dimen/mtrl_calendar_header_text_padding">

      <TextView
        android:id="@+id/mtrl_picker_title_text"
        style="?attr/materialCalendarHeaderTitle"
        android:layout_width="match_parent"
        androi