package com.blackneko.infrastructure.database.repository

import com.blackneko.application.TransactionRunner

abstract class BaseRepository(

    protected val transactionRunner: TransactionRunner
)