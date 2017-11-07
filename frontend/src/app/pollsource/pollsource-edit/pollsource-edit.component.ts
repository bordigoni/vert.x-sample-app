import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {Observable} from 'rxjs/Observable';
import {PollSource} from '../pollsource.model';
import {PollsourceService} from '../pollsource.service';

@Component({
  selector: 'app-pollsource-edit',
  templateUrl: './pollsource-edit.component.html',
  styleUrls: ['./pollsource-edit.component.css']
})
export class PollsourceEditComponent implements OnInit {

  private pollSource$: Observable<PollSource>;
  private clientId: string;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private service: PollsourceService) {
  }

  ngOnInit() {
    this.pollSource$ = this.route.paramMap.switchMap((params) => {
      this.clientId = params.get('clientId');
      let id = params.get('id');
      if (id == 'new') {
        return Observable.create(obs => {
          obs.next({clientId: this.clientId, delay: 1000});
          obs.complete()
        });
      } else {
        return this.service.get(this.clientId, id);
      }
    })
  }

  save(pollSource: PollSource) {
    this.service.save(this.clientId, pollSource).toPromise().then(() => this.back());
  }

  back() {
    this.router.navigate(['/client', this.clientId, 'pollsource']);
  }

}
