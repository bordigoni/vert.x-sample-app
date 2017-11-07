import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {HttpModule} from '@angular/http';
import {HomeComponent} from './home/home.component';
import {ClientModule} from './client/client.module';
import {AppRoutingModule} from './app-routing.module';
import {PageNotFoundComponent} from './page-not-found';
import {PollsourceModule} from './pollsource/pollsource.module';


@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    PageNotFoundComponent
  ],
  imports: [
    BrowserModule,
    HttpModule,
    ClientModule,
    PollsourceModule,
    AppRoutingModule
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
