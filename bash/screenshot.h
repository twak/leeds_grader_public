//
// Created by twak on 14/10/2019.
// DO NOT EDIT THIS FILE
//

#ifndef SCREENSHOT_H
#define SCREENSHOT_H

#include <QWidget>
#include <set>
#include "responsive_layout.h"
#include "responsive_label.h"
#include "responsive_window.h"
#include <fstream>
#include <iostream>

class Screenshot : public ResponsiveWindow {
    Q_OBJECT

public:
    Screenshot(std::string srcLocation) : srcLocation(srcLocation) {};
    void scrollAll(QObject *qw, bool bottom, std::set<std::string>* labels, int* numberOfResults);

public slots:
    void doScreenshot();

private:
    void showEvent(QShowEvent *);
    void schedule();
    std::vector<QSize> remaining;
    QTimer* mTimer;
    const std::string srcLocation;
    std::ofstream html;
};


#endif //SCREENSHOT_H
